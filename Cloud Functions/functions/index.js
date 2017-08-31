// The Cloud Functions for Firebase SDK to create Cloud Functions and setup triggers.
const functions = require('firebase-functions');

// The Firebase Admin SDK to access the Firebase Realtime Database. 
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

// Calculate Statistics
exports.calculateStatistics = functions.database.ref('/users/{userid}/records').onWrite(
    event => {
        const records = event.data.val();
        console.log('Processing User', event.params.userid);
        console.log('Calculating Total Mileage...');
        var total = calculateTotalMileage(records);
        console.log('Total Mileage: ', total, ' km');
        console.log('Finished Processing User');
        var stats = {totalMileage: total};
        return event.data.ref.parent.child('statistics').set(stats);
    }
)

function calculateTotalMileage(recordList) {
    var mileage = 0.0;
    Object.keys(recordList).forEach(key => {
        if (typeof recordList[key] === 'object') {
            mileage += parseFloat(recordList[key].totalMileage);
        }
    })
    return mileage;
}

function calculateByClass(recordList) {
    var classMileage = {};
    classMileage['class2'] = 0.0;
    classMileage['class3'] = 0.0;
    classMileage['class4'] = 0.0;
    classMileage['class4s'] = 0.0;
    classMileage['class5'] = 0.0;
    classMileage['class4a'] = 0.0;
    classMileage['class1'] = 0.0;
    classMileage['class3c'] = 0.0;
    Object.keys(recordList).forEach(key => {
        if (typeof recordList[key] === 'object') {
            classMileage[recordList[key].vehicleClass] += parseFloat(recordList[key].totalMileage);
        }
    })
    return classMileage;
}