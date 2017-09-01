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
        stats = Object.assign({}, stats, classM);
        console.log('Finished Calculating Class Total Mileage');
        console.log('Processing Mileage split by misc items (date, month, vehicle type)');
        var misc = miscMileage(records);
        stats.timeRecords = misc.time;
        stats.vehicleTypes = misc.vehicles;
        stats.vehicleNumberRecords = misc.vehicleNumber;
        console.log('Finished Processing Miscellaneous Mileage Processing');
        console.log(stats);
        console.log('Finished Processing User');
        return event.data.ref.parent.child('statistics').set(stats);
    }
)

function miscMileage(recordList) {
    var date = {};
    var month = {};
    var vehicle = {};
    var vehicleNum = {};
    Object.keys(recordList).forEach(key => {
        if (typeof recordList[key] === 'object') {
            if (recordList[key].trainingMileage == true) return;
            
            // Add Date Time mileage
            var rDate = new Date(recordList[key].dateTimeFrom);
            rDate.setHours(0,0,0,0); // Add to date
            if (!date[rDate.getTime().toString()]) date[rDate.getTime().toString()] = 0.0;
            date[rDate.getTime().toString()] += parseFloat(recordList[key].totalMileage);
            rDate.setDate(1); // Add to month
            if (!month[rDate.getTime().toString()]) month[rDate.getTime().toString()] = 0.0;
            month[rDate.getTime().toString()] += parseFloat(recordList[key].totalMileage);

            // Add Vehicle filtered mileage
            if (!vehicle[recordList[key].vehicleId]) vehicle[recordList[key].vehicleId] = 0.0;
            if (!vehicleNum[recordList[key].vehicleNumber]) vehicleNum[recordList[key].vehicleNumber] = 0.0;
            vehicle[recordList[key].vehicleId] += parseFloat(recordList[key].totalMileage); // Vehicle Type
            vehicleNum[recordList[key].vehicleNumber] += parseFloat(recordList[key].totalMileage); // Vehicle Number
        }
    });
    var result = {time: {perDate: date, perMonth: month}, vehicles: vehicle, vehicleNumber: vehicleNum}
    console.log(result);
    return result;
}

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