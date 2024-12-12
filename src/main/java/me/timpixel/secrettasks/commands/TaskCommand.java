package me.timpixel.secrettasks.commands;

import me.timpixel.fivelifes.commands.RootCommand;
import me.timpixel.fivelifes.commands.SubCommand;
import me.timpixel.secrettasks.TaskDistributor;

public class TaskCommand extends RootCommand {

    private final TaskDistributor taskDistributor;

    public TaskCommand(TaskDistributor taskDistributor) {
        this.taskDistributor = taskDistributor;
    }

    @Override
    protected SubCommand[] getSubCommands() {
        SubCommand[] commands = new SubCommand[5];
        commands[0] = new TaskGetCommand(this);
        commands[1] = new TaskRerollCommand(this);
        commands[2] = new TaskFailCommand(this);
        commands[3] = new TaskCompleteCommand(this);
        commands[4] = new TaskUnassignCommand(this);
        return commands;
    }

    public TaskDistributor getTaskDistributor() {
        return taskDistributor;
    }
}
