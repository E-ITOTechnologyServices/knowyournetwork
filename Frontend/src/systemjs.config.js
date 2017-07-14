/**
 * System configuration for Angular samples
 * Adjust as necessary for your application needs.
 */
(function (global) {
  System.config({
    paths: {
      // paths serve as alias
      'npm:': 'node_modules/'
    },
    // map tells the System loader where to look for things
    map: {
      // our app is within the app folder
      app: 'app',

      // Bootstrap
      'ng2-bootstrap/ng2-bootstrap': 'npm:ng2-bootstrap/bundles/ngx-bootstrap.umd.js',

      // Angular bundles
      '@angular/core': 'npm:@angular/core/bundles/core.umd.js',
      '@angular/common': 'npm:@angular/common/bundles/common.umd.js',
      '@angular/compiler': 'npm:@angular/compiler/bundles/compiler.umd.js',
      '@angular/platform-browser': 'npm:@angular/platform-browser/bundles/platform-browser.umd.js',
      '@angular/platform-browser-dynamic': 'npm:@angular/platform-browser-dynamic/bundles/platform-browser-dynamic.umd.js',
      '@angular/http': 'npm:@angular/http/bundles/http.umd.js',
      '@angular/router': 'npm:@angular/router/bundles/router.umd.js',
      '@angular/forms': 'npm:@angular/forms/bundles/forms.umd.js',

      // Other libraries
      'rxjs': 'npm:rxjs',
      'moment': 'npm:moment',
      'angular-in-memory-web-api': 'npm:angular-in-memory-web-api/bundles/in-memory-web-api.umd.js',
      'ng2-cookies': 'npm:ng2-cookies',
      'ng2-select': 'npm:ng2-select',

      // D3.js
      'd3': 'npm:d3',
      'underscore': 'npm:underscore',

      // Copy to clipboard
      'clipboard': 'npm:clipboard/dist/clipboard.js',
      'ngx-clipboard': 'npm:ngx-clipboard',

      // DateTime Picker
      'ng2-datetime-picker': 'npm:ng2-datetime-picker/dist'
    },
    // packages tells the System loader how to load when no filename and/or no extension
    packages: {
      app: {
        main: './main.js',
        defaultExtension: 'js'
      },
      rxjs: {
        defaultExtension: 'js'
      },
      d3: {
        main: './d3.js',
        defaultExtension: 'js'
      },
      underscore: {
        main: './underscore.js',
        defaultExtension: 'js'
      },
      'ng2-cookies': {
        defaultExtension: 'js'
      },
      'ng2-select': {
        defaultExtension: 'js'
      },
      'ngx-clipboard': {
        main: './dist/index.umd.js',
        defaultExtension: 'js'
      },
      'clipboard': {
        defaultExtension: 'js'
      },
      'ng2-datetime-picker': {
        main: 'ng2-datetime-picker.umd.js',
        defaultExtension: 'js'
      },
      'moment': {
        main: './moment.js',
        defaultExtension: 'js'
      }
    }
  });
})(this);
