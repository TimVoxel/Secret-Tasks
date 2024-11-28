package me.timpixel.secrettasks.resulttypes;

public enum TaskAssignmentResult {
    FailedHasTask,
    FailedIsDead,
    FailedIsRed,
    FailedNoRedTasksLoaded,
    FailedNoHardTasksLoaded,
    FailedNoAppropriateTasksFound,

    //Hard task specific
    FailedHasNoTask,
    FailedHasHardTask,

    Successful
}
