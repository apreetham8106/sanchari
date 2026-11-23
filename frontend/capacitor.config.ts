import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.sanchari.app',
  appName: 'Sanchari',
  webDir: 'dist/sanchari-app/browser',
  server: {
    androidScheme: 'https'
  }
};

export default config;