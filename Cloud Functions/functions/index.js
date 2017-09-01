// The Cloud Functions for Firebase SDK to create Cloud Functions and setup triggers.
const functions = require('firebase-functions');

// The Firebase Admin SDK to access the Firebase Realtime Database. 
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

// Calculate Statistics (non-training mileage only for now)
exports.calculateStatistics = functions.database.ref('/users/{userid}/records').onWrite(
    event => {
        const records = event.data.val();
        var stats = {totalMileage: 0}
        console.log('Processing User', event.params.userid);
        console.log('Calculating Total Mileage...');
        var total = calculateTotalMileage(records);
        stats.totalMileage = total;
        console.log('Total Mileage: ', total, ' km');
        console.log('Calculating Class Total Mileage...');
        // Only save those that has more than 0;
        var classM = calculateByClass(records);
        Object.keys(classM).forEach(key => {
            if (typeof classM[key] === 'object') {
                if (classM[key] > 0) stats[ classM[key] ] = classM[key];
            }
        })
        console.log('Finished Calculating Class Total Mileage');
        console.log(stats);
        console.log('Finished Processing User');
        return event.data.ref.parent.child('statistics').set(stats);
    }
)

function calculateTotalMileage(recordList) {
    var mileage = 0.0;
    Object.keys(recordList).forEach(key => {
        if (typeof recordList[key] === 'object') {
            if (recordList[key].trainingMileage == true) return;
            mileage += parseFloat(recordList[key].totalMileage);
        }
    });
    return mileage;
}

function calculateByClass(recordList) {
    var classMileage = {};
    Object.keys(recordList).forEach(key => {
        if (typeof recordList[key] === 'object') {
            if (recordList[key].trainingMileage == true) return;
            if (!classMileage[recordList[key].vehicleClass]) classMileage[recordList[key].vehicleClass] = 0.0;
            classMileage[recordList[key].vehicleClass] += parseFloat(recordList[key].totalMileage);
        }
    });
    return classMileage;
}