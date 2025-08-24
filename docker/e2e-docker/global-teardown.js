const { execSync } = require('child_process');

async function globalTeardown() {
  console.log('🧹 Cleaning up Docker containers after tests...');
  
  try {
    // Show container logs before cleanup (for debugging)
    if (process.env.SHOW_CONTAINER_LOGS === 'true') {
      console.log('📋 Container logs:');
      try {
        console.log('--- Frontend logs ---');
        execSync('docker logs tribe-frontend --tail 20', { stdio: 'inherit' });
        console.log('--- Backend logs ---');
        execSync('docker logs tribe-backend --tail 20', { stdio: 'inherit' });
        console.log('--- Zeebe logs ---');
        execSync('docker logs zeebe-broker --tail 20', { stdio: 'inherit' });
      } catch (logError) {
        console.warn('Could not retrieve container logs:', logError.message);
      }
    }

    // Stop and remove containers
    execSync('docker-compose -f docker-compose.test.yml down -v', { 
      stdio: 'inherit',
      cwd: __dirname + '/..'
    });
    
    console.log('✅ Docker cleanup completed');
    
  } catch (error) {
    console.error('❌ Error during cleanup:', error.message);
    // Don't throw error in teardown to avoid masking test failures
  }
}

module.exports = globalTeardown;