package me.timpixel.secrettasks.commands;

import me.timpixel.fivelifes.commands.RootCommand;
import me.timpixel.fivelifes.commands.SaveLifesCommand;
import me.timpixel.fivelifes.commands.SubCommand;
import me.timpixel.secrettasks.TaskBase;
import me.timpixel.secrettasks.TaskDistributor;

public class TasksCommand extends RootCommand {

    private final TaskBase taskBase;
    private final TaskDistributor taskDistributor;

    public TasksCommand(TaskBase taskBase, TaskDistributor taskDistributor) {
        this.taskBase = taskBase;
        this.taskDistributor = taskDistributor;
    }

    @Override
    protected SubCommand[] getSubCommands() {
        SubCommand[] commands = new SubCommand[4];
        commands[0] = new SaveTasksCommand(this);
        commands[1] = new FormatTasksCommand(this);
        commands[2] = new LoadTasksCommand(this);
        commands[3] = new PrintTasksCommand(this);
        return commands;
    }

    public TaskBase getTaskBase() {
        return taskBase;
    }

    public TaskDistributor getTaskDistributor() {
        return taskDistributor;
    }
}
