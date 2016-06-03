var printerPlugin = {
  printMessage: function(title, message, successCallback, errorCallback) {
    cordova.exec(
      successCallback,
      errorCallback,
      'PrinterPlugin',
      'printMessage',
      [{
        "title": title,
        "message": message
      }]
    );
  },
  scan: function(successCallback, errorCallback) {
    cordova.exec(
      successCallback,
      errorCallback,
      'PrinterPlugin',
      'scan',
      []
    );
  },
  connect: function(address,successCallback, errorCallback) {
    cordova.exec(
      successCallback,
      errorCallback,
      'PrinterPlugin',
      'connect',
      [{
        "address": address
      }]
    );
  },
  isBTOpen: function(successCallback, errorCallback) {
    cordova.exec(
      successCallback,
      errorCallback,
      'PrinterPlugin',
      'isBTOpen',
      []
    );
  },
  printImg: function(src, x, successCallback, errorCallback) {
    cordova.exec(
      successCallback,
      errorCallback,
      'PrinterPlugin',
      'printImg',
      [{
        'src': src,
        'x': x
      }]
    );
  },
  stopBT: function(successCallback, errorCallback) {
    cordova.exec(
      successCallback,
      errorCallback,
      'PrinterPlugin',
      'stopBT',
      []
    );
  },
  disconnect: function(){
    cordova.exec(
      function(){},
      function(){},
      'PrinterPlugin',
      'disconnect',
      []
    );
  },
  forceEnable: function(){
    cordova.exec(
      function(){},
      function(){},
      'PrinterPlugin',
      'forceEnable',
      []
    );
  },
};
module.exports = printerPlugin;
