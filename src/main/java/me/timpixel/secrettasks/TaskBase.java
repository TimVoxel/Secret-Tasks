package me.timpixel.secrettasks;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.logging.Level;

public class TaskBase {

    private final List<TaskMeta> tasks;
    private final List<UUID> usedTaskIds;

    private final List<TaskMeta> redTasks;
    private final List<TaskMeta> hardTasks;
    private final List<TaskMeta> normalGreenTasks;

    public TaskBase() {
        this.tasks = new ArrayList<>();
        this.usedTaskIds = new ArrayList<>();
        this.redTasks = new ArrayList<>();
        this.hardTasks  =new ArrayList<>();
        this.normalGreenTasks = new ArrayList<>();
    }

    public void save() {
        new Thread(this::saveSync).start();
    }

    public List<TaskMeta> getTasks() {
        return tasks;
    }

    public List<TaskMeta> getRedTasks() {
        return redTasks;
    }

    public List<TaskMeta> getNormalGreenTasks() {
        return normalGreenTasks;
    }

    public List<TaskMeta> getHardTasks() {
        return hardTasks;
    }

    public boolean isUsed(TaskMeta meta) {
        for (UUID uuid : usedTaskIds)
            if (uuid.equals(meta.getId()))
                return true;
        return false;
    }

    @SuppressWarnings("unchecked")
    private void saveSync() {
        JSONArray content = new JSONArray();

        for (TaskMeta task : tasks)
            content.add(task.serialize());

        try {
            FileWriter file = new FileWriter(new File(SecretTasks.getTasksSaveFile(), "tasks.json"));
            file.write(content.toJSONString());
            file.close();

        } catch (IOException e) {
            SecretTasks.log(Level.SEVERE, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public void saveUsedTasks() {
        new Thread(() -> {
            try {
                FileWriter file = new FileWriter(new File(SecretTasks.getTasksSaveFile(), "usedTasks.json"));
                JSONArray array = new JSONArray();

                for (UUID id : usedTaskIds)
                    array.add(id.toString());

                file.write(array.toJSONString());
                file.close();

            } catch (IOException e) {
                SecretTasks.log(Level.SEVERE, e.getMessage());
            }
        }).start();
    }

    public void registerTaskAsUsed(TaskMeta task) {
        usedTaskIds.add(task.getId());
    }

    private void loadUsedTasks() {
        usedTaskIds.clear();
        File file = new File(SecretTasks.getTasksSaveFile(), "usedTasks.json");

        try {
            JSONParser parser = new JSONParser();
            Reader reader = new FileReader(file);

            try {
                JSONArray tasksArray = (JSONArray) parser.parse(reader);

                for (Object entry : tasksArray) {
                    String id = (String) entry;
                    usedTaskIds.add(UUID.fromString(id));
                }
            }
            catch (ParseException e) {
                SecretTasks.log(Level.SEVERE, "ParseException: " + e.getMessage());
            }
            catch (IOException e) {
                SecretTasks.log(Level.SEVERE, "IOException: " + e.getMessage());
            }

            reader.close();
        }
        catch (FileNotFoundException e) {
            SecretTasks.log(Level.INFO, "Save file \"usedTasks.json\" not found, used tasks not loaded");
        }
        catch (IOException e) {
            SecretTasks.log(Level.SEVERE, e.getMessage());
        }
    }

    public boolean resetUsedTasks() {
        File file = new File(SecretTasks.getTasksSaveFile(), "usedTasks.json");
        boolean result = false;

        if (file.exists())
            try {
                result = file.delete();
            }
            catch (SecurityException exception) {
                SecretTasks.log(Level.SEVERE, exception.getMessage());
            }

        if (result)
            usedTaskIds.clear();

        return result;
    }

    public void print() {
        for (TaskMeta task : tasks) {
            SecretTasks.log(Level.INFO, "Task (id: " + task.getId().toString() + ")");
            SecretTasks.log(Level.INFO, "Text: \"" + task.getText() + "\"");
            SecretTasks.log(Level.INFO, "Unsuitable For: " + task.getUnsuitableFor() + "\n");
        }
    }

    public void format() {
        reset();

        new Thread(() -> {

            File file = new File(SecretTasks.getTasksSaveFile(), "tasks_unformated.txt");
            try {
                Reader reader = new FileReader(file);
                Scanner scanner = new Scanner(reader);

                List<UUID> currentListOfUnsuitableUUIDs = new ArrayList<>();
                String currentText = "";
                boolean isRed = false;
                boolean isHard = false;

                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();

                    if (line.isEmpty()) {

                        if (currentText.isEmpty())
                            continue;

                        UUID taskUUID = UUID.randomUUID();
                        TaskMeta taskMeta = new TaskMeta(currentText, taskUUID, currentListOfUnsuitableUUIDs, isRed, isHard);
                        tasks.add(taskMeta);

                        if (taskMeta.isRed())
                            redTasks.add(taskMeta);
                        else if (taskMeta.isHard())
                            hardTasks.add(taskMeta);
                        else
                            normalGreenTasks.add(taskMeta);

                        currentText = "";
                        currentListOfUnsuitableUUIDs = new ArrayList<>();
                        isRed = false;
                        isHard = false;
                    }
                    else {
                        if (line.equalsIgnoreCase("(красная)")) {
                            isRed = true;
                        }
                        else if (line.equalsIgnoreCase("(сложная)"))
                        {
                            isHard = true;
                        }
                        else if (line.charAt(0) == '!')
                            currentListOfUnsuitableUUIDs.add(UUID.fromString(line.substring(1)));
                        else
                            currentText = line;
                    }
                }

                if (!currentText.isEmpty())
                {
                    UUID taskUUID = UUID.randomUUID();
                    TaskMeta taskMeta = new TaskMeta(currentText, taskUUID, currentListOfUnsuitableUUIDs, isRed, isHard);
                    tasks.add(taskMeta);

                    if (taskMeta.isRed())
                        redTasks.add(taskMeta);
                    else if (taskMeta.isHard())
                        hardTasks.add(taskMeta);
                    else
                        normalGreenTasks.add(taskMeta);
                }

                reader.close();
            }
            catch (FileNotFoundException e) {
                SecretTasks.log(Level.SEVERE, "Save file \"tasks_unformated.txt\" not found, tasks not formated to json");
            }
            catch (IOException e) {
                SecretTasks.log(Level.SEVERE, e.getMessage());
            }

            saveSync();
        }).start();
    }

    public void load() {
        reset();
        File file = new File(SecretTasks.getTasksSaveFile(), "tasks.json");

        try {
            JSONParser parser = new JSONParser();
            Reader reader = new FileReader(file);

            try {
                JSONArray tasksArray = (JSONArray) parser.parse(reader);

                for (Object entry : tasksArray) {
                    JSONObject taskObject = (JSONObject) entry;
                    TaskMeta task = TaskMeta.deserialize(taskObject);

                    if (task.isRed())
                        redTasks.add(task);
                    else if (task.isHard())
                        hardTasks.add(task);
                    else
                        normalGreenTasks.add(task);

                    tasks.add(task);
                }
            }
            catch (IOException e) {
                SecretTasks.log(Level.SEVERE, "IOException while loading tasks: " + e.getMessage());
            }
            catch (ParseException e) {
                SecretTasks.log(Level.SEVERE, "ParseException while loading tasks: " + e.getMessage());
            }

            reader.close();
        }
        catch (FileNotFoundException e) {
            SecretTasks.log(Level.INFO, "Save file \"tasks.json\" not found, tasks not loaded");
        }
        catch (IOException e) {
            SecretTasks.log(Level.SEVERE, "2. IOException when loading tasks: " + e.getMessage());
        }

        loadUsedTasks();
    }

    private void reset() {
        tasks.clear();
        redTasks.clear();
        hardTasks.clear();
        normalGreenTasks.clear();
    }
}
