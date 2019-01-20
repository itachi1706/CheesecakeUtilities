package com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.model;

/**
 * Created by Kenneth on 14/1/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.model in CheesecakeUtilities
 */
public class MSLData {

    private Subjects[] subjects;
    private Task[] tasks;
    private Exam[] exams;

    public Subjects[] getSubjects() {
        return subjects;
    }

    public Task[] getTasks() {
        return tasks;
    }

    public Exam[] getExams() {
        return exams;
    }

    public class Subjects {
        private String guid, name;

        public String getGuid() {
            return guid;
        }

        public String getName() {
            return name;
        }
    }

    public class Task {
        private String guid, type, title, detail, due_date;
        private double timestamp;
        private String completed_at, subject_guid, exam_guid;
        private String examString;
        private int progress;

        public String getGuid() {
            return guid;
        }

        public String getType() {
            return type;
        }

        public String getTitle() {
            return title;
        }

        public String getDetail() {
            return detail;
        }

        public String getDue_date() {
            return due_date;
        }

        public double getTimestamp() {
            return timestamp;
        }

        public String getCompleted_at() {
            return completed_at;
        }

        public String getSubject_guid() {
            return subject_guid;
        }

        public int getProgress() {
            return progress;
        }

        // If revision

        public void setExamString(String examString) {
            this.examString = examString;
        }

        public String getExam_guid() {
            return exam_guid;
        }

        public String getExamString() {
            return examString;
        }
    }

    public class Exam {
        private String guid, module, date;
        private int duration;
        private boolean resit;
        private String seat, room, subject_guid;

        public String getGuid() {
            return guid;
        }

        public String getModule() {
            return module;
        }

        public String getDate() {
            return date;
        }

        public int getDuration() {
            return duration;
        }

        public boolean isResit() {
            return resit;
        }

        public String getSeat() {
            return seat;
        }

        public String getRoom() {
            return room;
        }

        public String getSubject_guid() {
            return subject_guid;
        }
    }
}
