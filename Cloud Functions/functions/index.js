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