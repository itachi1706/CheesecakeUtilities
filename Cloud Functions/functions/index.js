// The Cloud Functions for Firebase SDK to create Cloud Functions and setup triggers.
const functions = require('firebase-functions');

// The Firebase Admin SDK to access the Firebase Realtime Database. 
const admin = require('firebase-admin');
admin.initializeApp();

// Vehicle Mileage Utility
// Calculate Statistics (non-training mileage only for now)
exports.calculateStatistics = functions.database.ref('/vehmileage/users/{userid}/records').onWrite(
    (snapshot, context) => {
        console.log("Function Version: 030620191912")
        console.log("Dep Versions listed below");
        console.log(process.versions)
        const records = snapshot.after.val();
        var stats = {totalMileage: 0}
        console.log('Processing User', context.params.userid);
        console.log('Calculating Total Mileage...');
        var total = calculateTotalMileage(records);
        stats.totalMileage = total;
        console.log('Total Mileage: ', total, ' km');
        console.log('Calculating Class Total Mileage...');
        // Only save those that has more than 0;
        var classM = calculateByClass(records);
        stats = Object.assign({}, stats, classM);
        console.log('Processing Mileage split by misc items (date, month, vehicle type)...');
        var misc = miscMileage(records);
        stats.timeRecords = misc.time;
        stats.vehicleTypes = misc.vehicles;
        stats.vehicleNumberRecords = misc.vehicleNumber;
        console.log('Finished Processing User. Saving to Firebase DB');
        console.log(stats);
        return setRecord(snapshot.after.ref.parent.child('statistics'), stats, context);
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
            var timezoneOffset = 8 * 60 * 60 * 1000; // 8 Hours
            if (recordList[key].hasOwnProperty("timezone")) timezoneOffset = recordList[key].timezone; // Adjust date to timezone
            var rDate = new Date(parseInt(recordList[key].datetimeFrom, 10) + timezoneOffset); // Init with timezone difference
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

function setRecord(ref, record, context) {
    const appOptions = JSON.parse(process.env.FIREBASE_CONFIG);
    appOptions.databaseAuthVariableOverride = context.auth;
    const app = admin.initializeApp(appOptions, 'app');

    const deleteApp = () => app.delete().catch(() => null);
    return app.database().ref(ref).set(record).then(res => {
        // Deleting the app is necessary for preventing concurrency leaks
        return deleteApp().then(() => res);
      }).catch(err => {
        return deleteApp().then(() => Promise.reject(err));
      });
}

// GPA Calculator Utility
// Calculate GPA/Score (Also need to make sure it does not activate twice lol)
exports.calculateGpa = functions.database.ref('/gpacalc/users/{userid}').onWrite(
    async (snapshot, context) => {
        console.log("Function Version: beta-4-async");
        console.log("Dep Versions listed below");
        console.log(process.versions);
        const records = snapshot.after.val();
        const dataSnapshot = await admin.database().ref('/gpacalc/scoring').once('value');
        var stats = records;
        var gradeTiers = dataSnapshot.val();
        console.log('Processing User', context.params.userid);
        console.log('Calculating GPA of the various institutions...');
        Object.keys(records).forEach(key => {
            if (typeof records[key] === 'object') {
                console.log("Processing ", records[key].name, " (", records[key].shortName, ")");
                stats[key] = processInstitution(records[key], gradeTiers[records[key].type]);
            }
        });
        console.log('Finished Processing User. Saving to Firebase DB');
        console.log(stats);
        return setRecord(snapshot.after.ref, stats, context); // TODO: Update to point to ref only
    }
)

function processInstitution(institution, gradeTier) {
    // Process all the semesters first
    if (institution.semester == null) {
        institution.gpa = "Unknown"
        if (gradeTier.type == "gpa") institution.totalCredits = 0 // Total Credits is only gradable credits
        return institution; // Don't edit anything
    }
    var count = 0.0;
    var totalCredits = 0.0;
    Object.keys(institution.semester).forEach(key => {
        if (typeof institution.semester[key] === 'object') {
            console.log("Processing Semester: ", institution.semester[key].name)
            institution.semester[key] = processSemester(institution.semester[key], gradeTier);
            // Process count or semester
            if (institution.semester[key].gpa == "Unknown") return; // Do not try and add unknown lol
            if (gradeTier.type == "count") {
                // Add all the semesters up
                count += parseInt(institution.semester[key].gpa);
            } else {
                var sem_gpa = institution.semester[key].fullgpa * institution.semester[key].totalCredits;
                count += sem_gpa; 
                totalCredits += institution.semester[key].totalCredits;
            }
        }
    });
    console.log("Institution Count and Credits: ", count, " | ", totalCredits);
    if (gradeTier.type == "count") {
        institution.gpa = parseInt(count, 10) + "";
    } else if (totalCredits != 0) {
        var gpa = count / totalCredits;
        institution.fullgpa = gpa;
        institution.totalCredits = parseInt(totalCredits);
        institution.gpa = toFixed(gpa, 4);
    } else {
        institution.gpa = "Unknown";
        institution.totalCredits = 0;
    }
    return institution;
}

function processSemester(semester, gradeTier) {
    // Calculate the various modules
    if (semester.modules == null) {
        semester.gpa = "Unknown"
        if (gradeTier.type == "gpa") semester.totalCredits = 0 // Total Credits is only gradable credits
        return semester;
    }
    var count = 0.0;
    var totalCredits = 0.0;
    Object.keys(semester.modules).forEach(key => {
        if (typeof semester.modules[key] === 'object') {
            if (semester.modules[key].passFail == true) {
                console.log("Ignoring Pass/Fail Module [", semester.modules[key].courseCode, "] ", semester.modules[key].name);
                return;
            }
            if (semester.modules[key].gradeTier == -1) {
                console.log("Ignoring module [", semester.modules[key].courseCode, "] ", semester.modules[key].name, " without grades");
                return;
            }
            var grade = gradeTier.gradetier[semester.modules[key].gradeTier].value; 
            var mod_gpa = grade * semester.modules[key].credits; // GPA Calculation = (grade * credits) / total credits
            console.log("Grade Received for [", semester.modules[key].courseCode, "] ", semester.modules[key].name, ": ", grade, " | Module Grade with Credits: ", mod_gpa);
            count += mod_gpa; 
            totalCredits += semester.modules[key].credits;
        }
    });
    console.log("Semester Count and Credits: ", count, " | ", totalCredits);
    if (gradeTier.type == "count") {
        // Is count so we return the count only
        semester.gpa = parseInt(count, 10) + "";
    } else if (totalCredits != 0) {
        var gpa = count / totalCredits; 
        semester.fullgpa = gpa;
        semester.gpa = toFixed(gpa, 4);
        semester.totalCredits = parseInt(totalCredits);
    } else {
        semester.gpa = "Unknown";
        semester.totalCredits = 0;
    }
    return semester;
}

function toFixed(num, fixed) {
    var re = new RegExp('^-?\\d+(?:\.\\d{0,' + (fixed || -1) + '})?');
    return num.toString().match(re)[0];
}