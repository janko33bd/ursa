const { execSync } = require('child_process');

async function globalSetup() {
  console.log('🚀 Starting Docker containers for E2E tests...');
  
  try {
    // Stop any existing containers
    console.log('📦 Stopping existing containers...');
    try {
      execSync('docker-compose -f docker-compose.test.yml down', { 
        stdio: 'inherit',
        cwd: __dirname + '/..'
      });
    } catch (e) {
      // Ignore errors from stopping non-existent containers
    }

    // Start containers
    console.log('🏗️  Building and starting containers...');
    execSync('docker-compose -f docker-compose.test.yml up -d --build', { 
      stdio: 'inherit',
      cwd: __dirname + '/..'
    });

    // Wait for services to be ready
    console.log('⏳ Waiting for services to be ready...');
    
    // Give Zeebe extra time to initialize (it doesn't have health check)
    console.log('⏳ Waiting for Zeebe to initialize (60 seconds)...');
    await new Promise(resolve => setTimeout(resolve, 60000));
    
    let attempts = 0;
    const maxAttempts = 30; // 2.5 minutes additional wait
    
    while (attempts < maxAttempts) {
      try {
        const healthCheck = execSync('docker-compose -f docker-compose.test.yml ps --format json', { 
          cwd: __dirname + '/..',
          encoding: 'utf8'
        });
        
        const services = healthCheck.split('\n')
          .filter(line => line.trim())
          .map(line => JSON.parse(line));
        
        // Check if all services are running (Zeebe doesn't have health check)
        const allRunning = services.every(service => 
          service.State === 'running' || service.Health === 'healthy'
        );
        
        if (allRunning && services.length >= 3) {
          console.log('✅ All services are running!');
          
          // Test backend health specifically
          try {
            execSync('curl -f http://localhost:8080/actuator/health', { 
              stdio: 'inherit',
              cwd: __dirname + '/..'
            });
            console.log('✅ Backend is healthy!');
          } catch (healthError) {
            console.warn('⚠️  Backend not healthy yet, will continue anyway');
          }
          
          return;
        }
        
        console.log(`⏳ Waiting for services... (attempt ${attempts + 1}/${maxAttempts})`);
        await new Promise(resolve => setTimeout(resolve, 5000));
        attempts++;
        
      } catch (error) {
        console.log(`⏳ Services not ready yet... (attempt ${attempts + 1}/${maxAttempts})`);
        await new Promise(resolve => setTimeout(resolve, 5000));
        attempts++;
      }
    }
    
    if (attempts >= maxAttempts) {
      console.error('❌ Timeout waiting for services to be ready');
      console.log('📊 Current container status:');
      try {
        execSync('docker-compose -f docker-compose.test.yml ps', { 
          stdio: 'inherit',
          cwd: __dirname + '/..'
        });
      } catch (e) {
        console.error('Failed to get container status');
      }
      throw new Error('Services did not become ready in time');
    }
    
  } catch (error) {
    console.error('❌ Failed to start Docker containers:', error.message);
    throw error;
  }
}

module.exports = globalSetup;