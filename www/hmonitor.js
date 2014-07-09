var Hmonitor = {
    checkBt: function(userid, otherparam, successCallback, errorCallback) {
    	cordova.exec(
                successCallback, // success callback function
                errorCallback, // error callback function
                'Hmonitor', // mapped to our native Java class called "Calendar"
                'checkBt', // with this action name
                [{                  // and this array of custom arguments to create our entry
                    "userid": userid,
                    "otherparam": otherparam                   
                }]
            ); 
    },
    checkConnection: function(userid, otherparam, successCallback, errorCallback) {
    	cordova.exec(
                successCallback, // success callback function
                errorCallback, // error callback function
                'Hmonitor', // mapped to our native Java class called "Calendar"
                'checkConnection', // with this action name
                [{                  // and this array of custom arguments to create our entry
                    "userid": userid,
                    "otherparam": otherparam                   
                }]
            ); 
    }
    
}
module.exports = Hmonitor;