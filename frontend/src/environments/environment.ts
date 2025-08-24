export const environment = {
  production: false,
  // In development, we point directly to the Spring Boot backend API.
  // In production, this will be an empty string, and NGINX will handle the routing.
  apiURL: 'http://localhost:8080'
};
