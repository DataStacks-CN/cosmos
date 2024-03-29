package com.weibo.dip.cosmos.node.client;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.weibo.dip.cosmos.Conf;
import com.weibo.dip.cosmos.model.Application;
import com.weibo.dip.cosmos.model.ApplicationDependency;
import com.weibo.dip.cosmos.model.ApplicationRecord;
import com.weibo.dip.cosmos.model.ApplicationState;
import com.weibo.dip.cosmos.model.Message;
import com.weibo.dip.cosmos.model.ScheduleApplication;
import com.weibo.dip.cosmos.node.util.ConsoleTable;
import com.weibo.dip.durian.ClasspathProperties;
import com.weibo.dip.durian.Symbols;
import com.weibo.dip.durian.util.GsonUtil;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.StringUtils;

/** Console node service. */
public class ConsoleClient {

  private static final ClasspathProperties PROPERTIES;

  static {
    try {
      PROPERTIES = new ClasspathProperties(Conf.COSMOS_PROPERTIES);
    } catch (Exception e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  private static CosmosClient getClient() throws Exception {
    String[] hosts = PROPERTIES.getString("server.hosts").split(Symbols.COMMA);
    Preconditions.checkState(
        ArrayUtils.isNotEmpty(hosts), "server.hosts must be specified in node.properties");

    int port = PROPERTIES.getInt("server.port");

    for (String host : hosts) {
      CosmosClient client = new CosmosClient(host, port);

      if (client.connect()) {
        return client;
      }
    }

    return null;
  }

  /**
   * Main.
   *
   * @param args command line
   * @throws Exception if error
   */
  public static void main(String[] args) throws Exception {
    OptionGroup group = new OptionGroup();

    group.addOption(
        Option.builder(Conf.OPTION_ADD).hasArg(true).argName("add.json").required(false).build());
    group.addOption(
        Option.builder(Conf.OPTION_UPDATE)
            .hasArg(true)
            .argName("update.json")
            .required(false)
            .build());
    group.addOption(
        Option.builder(Conf.OPTION_DELETE)
            .hasArg(true)
            .argName("name:queue")
            .required(false)
            .build());

    group.addOption(
        Option.builder(Conf.OPTION_START).hasArg(true).argName("app.json").required(false).build());
    group.addOption(
        Option.builder(Conf.OPTION_STOP)
            .hasArg(true)
            .argName("name:queue")
            .required(false)
            .build());
    group.addOption(
        Option.builder(Conf.OPTION_CALL).hasArg(true).argName("call.json").required(false).build());
    group.addOption(
        Option.builder(Conf.OPTION_APP).hasArg(true).argName("name:queue").required(false).build());
    group.addOption(
        Option.builder(Conf.OPTION_QUEUES)
            .hasArg(false)
            .desc("list queues")
            .required(false)
            .build());
    group.addOption(
        Option.builder(Conf.OPTION_APPS)
            .hasArg(true)
            .argName("queue")
            .desc("list apps by queue")
            .required(false)
            .build());
    group.addOption(
        Option.builder(Conf.OPTION_RUNNING)
            .hasArg(true)
            .argName("queue")
            .desc("list running apps by queue")
            .required(false)
            .build());
    group.addOption(
        Option.builder(Conf.OPTION_ADD_DEPEND)
            .hasArg(true)
            .argName("depend.json")
            .desc("add dependency")
            .required(false)
            .build());
    group.addOption(
        Option.builder(Conf.OPTION_REMOVE_DEPEND)
            .hasArg(true)
            .argName("depend.json")
            .desc("remove dependency")
            .required(false)
            .build());
    group.addOption(
        Option.builder(Conf.OPTION_GET_DEPENDS)
            .hasArg(true)
            .argName("name:queue")
            .desc("get dependencies")
            .required(false)
            .build());
    group.addOption(
        Option.builder(Conf.OPTION_QUEUED)
            .hasArg(false)
            .desc("list queued apps")
            .required(false)
            .build());
    group.addOption(
        Option.builder(Conf.OPTION_RECORDS)
            .hasArg(true)
            .argName("records.json")
            .desc("list app records")
            .required(false)
            .build());
    group.addOption(
        Option.builder(Conf.OPTION_REPAIR)
            .hasArg(true)
            .argName("repair.json")
            .desc("repair app")
            .required(false)
            .build());
    group.addOption(
        Option.builder(Conf.OPTION_REPLAY)
            .hasArg(true)
            .argName("replay.json")
            .desc("replay app")
            .required(false)
            .build());
    group.addOption(
        Option.builder(Conf.OPTION_DELETE_QUEUED)
            .hasArg(true)
            .argName("message id")
            .desc("delete queued app")
            .required(false)
            .build());
    group.addOption(
        Option.builder(Conf.OPTION_KILL)
            .hasArg(true)
            .argName("kill.json")
            .desc("kill app")
            .required(false)
            .build());
    group.addOption(
        Option.builder(Conf.OPTION_LOG)
            .hasArg(true)
            .argName("log.json")
            .desc("log app")
            .required(false)
            .build());
    group.addOption(Option.builder("help").hasArg(false).required(false).build());

    Options options = new Options();

    options.addOptionGroup(group);

    HelpFormatter formatter = new HelpFormatter();

    if (ArrayUtils.isEmpty(args)) {
      formatter.printHelp("COMMAND", options);
      return;
    }

    CommandLineParser parser = new DefaultParser();

    CommandLine line;

    try {
      line = parser.parse(options, args);
    } catch (ParseException e) {
      System.out.println("Error: " + e.getMessage());

      formatter.printHelp("COMMAND", options);
      return;
    }

    CosmosClient client = getClient();

    assert Objects.nonNull(client);

    if (line.hasOption(Conf.OPTION_ADD)) {
      String jsonPath = line.getOptionValue(Conf.OPTION_ADD);

      String json =
          StringUtils.join(
              FileUtils.readLines(new File(jsonPath), CharEncoding.UTF_8), Symbols.NEWLINE);

      JsonParser jsonParser = new JsonParser();

      JsonObject jsonObject = jsonParser.parse(json).getAsJsonObject();

      String name = jsonObject.getAsJsonPrimitive("name").getAsString();
      String queue = jsonObject.getAsJsonPrimitive("queue").getAsString();
      String user = jsonObject.getAsJsonPrimitive("user").getAsString();
      int priority = jsonObject.getAsJsonPrimitive("priority").getAsInt();
      int cores = jsonObject.getAsJsonPrimitive("cores").getAsInt();
      int mems = jsonObject.getAsJsonPrimitive("mems").getAsInt();
      String repository = jsonObject.getAsJsonPrimitive("repository").getAsString();
      String tag = jsonObject.getAsJsonPrimitive("tag").getAsString();
      String params = jsonObject.getAsJsonObject("params").toString();
      String cron = jsonObject.getAsJsonPrimitive("cron").getAsString();
      int timeout = jsonObject.getAsJsonPrimitive("timeout").getAsInt();

      client.add(
          new Application(
              name, queue, user, priority, cores, mems, repository, tag, params, cron, timeout));

      System.out.println(String.format("Application %s:%s added", name, queue));
    } else if (line.hasOption(Conf.OPTION_UPDATE)) {
      String jsonPath = line.getOptionValue(Conf.OPTION_UPDATE);

      String json =
          StringUtils.join(
              FileUtils.readLines(new File(jsonPath), CharEncoding.UTF_8), Symbols.NEWLINE);

      JsonParser jsonParser = new JsonParser();

      JsonObject jsonObject = jsonParser.parse(json).getAsJsonObject();

      String name = jsonObject.getAsJsonPrimitive("name").getAsString();
      String queue = jsonObject.getAsJsonPrimitive("queue").getAsString();
      String user = jsonObject.getAsJsonPrimitive("user").getAsString();
      int priority = jsonObject.getAsJsonPrimitive("priority").getAsInt();
      int cores = jsonObject.getAsJsonPrimitive("cores").getAsInt();
      int mems = jsonObject.getAsJsonPrimitive("mems").getAsInt();
      String repository = jsonObject.getAsJsonPrimitive("repository").getAsString();
      String tag = jsonObject.getAsJsonPrimitive("tag").getAsString();
      String params = jsonObject.getAsJsonObject("params").toString();
      String cron = jsonObject.getAsJsonPrimitive("cron").getAsString();
      int timeout = jsonObject.getAsJsonPrimitive("timeout").getAsInt();

      client.update(
          new Application(
              name, queue, user, priority, cores, mems, repository, tag, params, cron, timeout));

      System.out.println(String.format("Application %s:%s updated", name, queue));
    } else if (line.hasOption(Conf.OPTION_DELETE)) {
      String nameAndQueue = line.getOptionValue(Conf.OPTION_DELETE);

      String name = nameAndQueue.split(Symbols.COLON)[0];
      String queue = nameAndQueue.split(Symbols.COLON)[1];

      client.delete(name, queue);

      System.out.println(String.format("Application %s:%s deleted", name, queue));
    } else if (line.hasOption(Conf.OPTION_START)) {
      String nameAndQueue = line.getOptionValue(Conf.OPTION_START);

      String name = nameAndQueue.split(Symbols.COLON)[0];
      String queue = nameAndQueue.split(Symbols.COLON)[1];

      client.start(name, queue);

      System.out.println(String.format("Application %s:%s started", name, queue));
    } else if (line.hasOption(Conf.OPTION_STOP)) {
      String nameAndQueue = line.getOptionValue(Conf.OPTION_STOP);

      String name = nameAndQueue.split(Symbols.COLON)[0];
      String queue = nameAndQueue.split(Symbols.COLON)[1];

      client.stop(name, queue);

      System.out.println(String.format("Application %s:%s stoped", name, queue));
    } else if (line.hasOption(Conf.OPTION_CALL)) {
      String jsonPath = line.getOptionValue(Conf.OPTION_CALL);

      String json =
          StringUtils.join(
              FileUtils.readLines(new File(jsonPath), CharEncoding.UTF_8), Symbols.NEWLINE);

      JsonParser jsonParser = new JsonParser();

      JsonObject jsonObject = jsonParser.parse(json).getAsJsonObject();

      String name = jsonObject.getAsJsonPrimitive("name").getAsString();
      String queue = jsonObject.getAsJsonPrimitive("queue").getAsString();

      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

      Date timestamp = format.parse(jsonObject.getAsJsonPrimitive("timestamp").getAsString());

      String jsonParams = jsonObject.getAsJsonObject("params").toString();

      client.call(name, queue, timestamp, jsonParams);

      System.out.println(
          String.format("Application %s:%s %s called", name, queue, format.format(timestamp)));
    } else if (line.hasOption(Conf.OPTION_APP)) {
      String nameAndQueue = line.getOptionValue(Conf.OPTION_APP);

      String name = nameAndQueue.split(Symbols.COLON)[0];
      String queue = nameAndQueue.split(Symbols.COLON)[1];

      Application application = client.get(name, queue);

      ConsoleTable table = new ConsoleTable();

      table.addRow(
          "name",
          "queue",
          "user",
          "priority",
          "cores",
          "mems(MB)",
          "repository",
          "tag",
          "params",
          "cron",
          "timeout(s)",
          "state");

      if (Objects.nonNull(application)) {
        table.addRow(
            application.getName(),
            application.getQueue(),
            application.getUser(),
            String.valueOf(application.getPriority()),
            String.valueOf(application.getCores()),
            String.valueOf(application.getMems()),
            application.getRepository(),
            application.getTag(),
            ArrayUtils.toString(application.getParams()),
            application.getCron(),
            String.valueOf(application.getTimeout()),
            client.isScheduled(application.getName(), application.getQueue())
                ? "started"
                : "stoped");
      }

      table.print();
    } else if (line.hasOption(Conf.OPTION_QUEUES)) {
      List<String> queues = client.queues();

      ConsoleTable table = new ConsoleTable();

      table.addRow("queues");

      if (CollectionUtils.isNotEmpty(queues)) {
        for (String queue : queues) {
          table.addRow(queue);
        }
      }

      table.print();
    } else if (line.hasOption(Conf.OPTION_APPS)) {
      String queue = line.getOptionValue(Conf.OPTION_APPS);

      List<Application> applications = client.list(queue);

      ConsoleTable table = new ConsoleTable();

      table.addRow(
          "name",
          "queue",
          "user",
          "priority",
          "cores",
          "mems(MB)",
          "repository",
          "tag",
          "params",
          "cron",
          "timeout(s)",
          "state");

      if (CollectionUtils.isNotEmpty(applications)) {
        for (Application application : applications) {
          table.addRow(
              application.getName(),
              application.getQueue(),
              application.getUser(),
              String.valueOf(application.getPriority()),
              String.valueOf(application.getCores()),
              String.valueOf(application.getMems()),
              application.getRepository(),
              application.getTag(),
              ArrayUtils.toString(application.getParams()),
              application.getCron(),
              String.valueOf(application.getTimeout()),
              client.isScheduled(application.getName(), application.getQueue())
                  ? "started"
                  : "stoped");
        }
      }

      table.print();
    } else if (line.hasOption(Conf.OPTION_RUNNING)) {
      String queue = line.getOptionValue(Conf.OPTION_RUNNING);

      List<ApplicationRecord> records = client.listRunning(queue);

      ConsoleTable table = new ConsoleTable();

      table.addRow("name", "queue", "host", "scheduleTime", "updateTime", "state");

      if (CollectionUtils.isNotEmpty(records)) {
        for (ApplicationRecord record : records) {
          table.addRow(
              record.getName(),
              record.getQueue(),
              record.getHost(),
              String.valueOf(record.getScheduleTime()),
              String.valueOf(record.getUpdateTime()),
              record.getState().name());
        }
      }

      table.print();
    } else if (line.hasOption(Conf.OPTION_ADD_DEPEND)) {
      String jsonPath = line.getOptionValue(Conf.OPTION_ADD_DEPEND);

      String json =
          StringUtils.join(
              FileUtils.readLines(new File(jsonPath), CharEncoding.UTF_8), Symbols.NEWLINE);

      JsonParser jsonParser = new JsonParser();

      JsonObject jsonObject = jsonParser.parse(json).getAsJsonObject();

      String name = jsonObject.getAsJsonPrimitive("name").getAsString();
      String queue = jsonObject.getAsJsonPrimitive("queue").getAsString();
      String dependName = jsonObject.getAsJsonPrimitive("dependName").getAsString();
      String dependQueue = jsonObject.getAsJsonPrimitive("dependQueue").getAsString();
      int fromSeconds = jsonObject.getAsJsonPrimitive("fromSeconds").getAsInt();
      int toSeconds = jsonObject.getAsJsonPrimitive("toSeconds").getAsInt();

      client.addDependency(
          new ApplicationDependency(name, queue, dependName, dependQueue, fromSeconds, toSeconds));

      System.out.println(
          "Dependency ("
              + name
              + Symbols.COLON
              + queue
              + ", "
              + dependName
              + Symbols.COLON
              + dependQueue
              + ") added");
    } else if (line.hasOption(Conf.OPTION_REMOVE_DEPEND)) {
      String jsonPath = line.getOptionValue(Conf.OPTION_REMOVE_DEPEND);

      String json =
          StringUtils.join(
              FileUtils.readLines(new File(jsonPath), CharEncoding.UTF_8), Symbols.NEWLINE);

      JsonParser jsonParser = new JsonParser();

      JsonObject jsonObject = jsonParser.parse(json).getAsJsonObject();

      String name = jsonObject.getAsJsonPrimitive("name").getAsString();
      String queue = jsonObject.getAsJsonPrimitive("queue").getAsString();
      String dependName = jsonObject.getAsJsonPrimitive("dependName").getAsString();
      String dependQueue = jsonObject.getAsJsonPrimitive("dependQueue").getAsString();

      client.removeDependency(name, queue, dependName, dependQueue);

      System.out.println(
          "Dependency ("
              + name
              + Symbols.COLON
              + queue
              + ", "
              + dependName
              + Symbols.COLON
              + dependQueue
              + ") removed");
    } else if (line.hasOption(Conf.OPTION_GET_DEPENDS)) {
      String nameAndQueue = line.getOptionValue(Conf.OPTION_GET_DEPENDS);

      String name = nameAndQueue.split(Symbols.COLON)[0];
      String queue = nameAndQueue.split(Symbols.COLON)[1];

      List<ApplicationDependency> dependencies = client.getDependencies(name, queue);

      ConsoleTable table = new ConsoleTable();

      table.addRow("name", "queue", "dependName", "dependQueue", "fromSeconds", "toSeconds");

      if (CollectionUtils.isNotEmpty(dependencies)) {
        for (ApplicationDependency dependency : dependencies) {
          table.addRow(
              dependency.getName(),
              dependency.getQueue(),
              dependency.getDependName(),
              dependency.getDependQueue(),
              String.valueOf(dependency.getFromSeconds()),
              String.valueOf(dependency.getToSeconds()));
        }
      }

      table.print();
    } else if (line.hasOption(Conf.OPTION_QUEUED)) {
      List<Message> messages = client.queued();

      ConsoleTable table = new ConsoleTable();

      table.addRow("mid", "name", "queue", "priority", "scheduleTime", "updateTime", "timestamp");

      if (CollectionUtils.isNotEmpty(messages)) {
        for (Message message : messages) {
          ScheduleApplication scheduleApplication =
              GsonUtil.fromJson(message.getMessage(), ScheduleApplication.class);

          table.addRow(
              String.valueOf(message.getId()),
              scheduleApplication.getName(),
              scheduleApplication.getQueue(),
              String.valueOf(message.getPriority()),
              String.valueOf(scheduleApplication.getApplicationRecord().getScheduleTime()),
              String.valueOf(scheduleApplication.getApplicationRecord().getUpdateTime()),
              String.valueOf(message.getTimestamp()));
        }
      }

      table.print();
    } else if (line.hasOption(Conf.OPTION_RECORDS)) {
      String jsonPath = line.getOptionValue(Conf.OPTION_RECORDS);

      String json =
          StringUtils.join(
              FileUtils.readLines(new File(jsonPath), CharEncoding.UTF_8), Symbols.NEWLINE);

      JsonParser jsonParser = new JsonParser();

      JsonObject jsonObject = jsonParser.parse(json).getAsJsonObject();

      String name = jsonObject.getAsJsonPrimitive("name").getAsString();
      String queue = jsonObject.getAsJsonPrimitive("queue").getAsString();
      String beginTime = jsonObject.getAsJsonPrimitive("beginTime").getAsString();
      String endTime = jsonObject.getAsJsonPrimitive("endTime").getAsString();

      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

      List<ApplicationRecord> records =
          client.listRecords(name, queue, format.parse(beginTime), format.parse(endTime));

      ConsoleTable table = new ConsoleTable();

      table.addRow("name", "queue", "host", "scheduleTime", "updateTime", "state");

      if (CollectionUtils.isNotEmpty(records)) {
        for (ApplicationRecord record : records) {
          table.addRow(
              record.getName(),
              record.getQueue(),
              record.getHost(),
              String.valueOf(record.getScheduleTime()),
              String.valueOf(record.getUpdateTime()),
              record.getState().name());
        }
      }

      table.print();
    } else if (line.hasOption(Conf.OPTION_REPAIR)) {
      String jsonPath = line.getOptionValue(Conf.OPTION_REPAIR);

      String json =
          StringUtils.join(
              FileUtils.readLines(new File(jsonPath), CharEncoding.UTF_8), Symbols.NEWLINE);

      JsonParser jsonParser = new JsonParser();

      JsonObject jsonObject = jsonParser.parse(json).getAsJsonObject();

      String name = jsonObject.getAsJsonPrimitive("name").getAsString();
      String queue = jsonObject.getAsJsonPrimitive("queue").getAsString();
      String beginTime = jsonObject.getAsJsonPrimitive("beginTime").getAsString();
      String endTime = jsonObject.getAsJsonPrimitive("endTime").getAsString();

      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

      List<ScheduleApplication> scheduleApplications =
          client.repair(name, queue, format.parse(beginTime), format.parse(endTime));

      ConsoleTable table = new ConsoleTable();

      table.addRow("name", "queue", "scheduleTime");

      if (CollectionUtils.isNotEmpty(scheduleApplications)) {
        for (ScheduleApplication scheduleApplication : scheduleApplications) {
          table.addRow(
              scheduleApplication.getName(),
              scheduleApplication.getQueue(),
              String.valueOf(scheduleApplication.getApplicationRecord().getScheduleTime()));
        }
      }

      table.print();
    } else if (line.hasOption(Conf.OPTION_REPLAY)) {
      String jsonPath = line.getOptionValue(Conf.OPTION_REPLAY);

      String json =
          StringUtils.join(
              FileUtils.readLines(new File(jsonPath), CharEncoding.UTF_8), Symbols.NEWLINE);

      JsonParser jsonParser = new JsonParser();

      JsonObject jsonObject = jsonParser.parse(json).getAsJsonObject();

      String name = jsonObject.getAsJsonPrimitive("name").getAsString();
      String queue = jsonObject.getAsJsonPrimitive("queue").getAsString();
      String beginTime = jsonObject.getAsJsonPrimitive("beginTime").getAsString();
      String endTime = jsonObject.getAsJsonPrimitive("endTime").getAsString();

      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

      List<ScheduleApplication> scheduleApplications =
          client.reply(name, queue, format.parse(beginTime), format.parse(endTime));

      ConsoleTable table = new ConsoleTable();

      table.addRow("name", "queue", "scheduleTime");

      if (CollectionUtils.isNotEmpty(scheduleApplications)) {
        for (ScheduleApplication scheduleApplication : scheduleApplications) {
          table.addRow(
              scheduleApplication.getName(),
              scheduleApplication.getQueue(),
              String.valueOf(scheduleApplication.getApplicationRecord().getScheduleTime()));
        }
      }

      table.print();
    } else if (line.hasOption(Conf.OPTION_DELETE_QUEUED)) {
      int id = Integer.valueOf(line.getOptionValue(Conf.OPTION_DELETE_QUEUED));

      System.out.println("deleted " + client.deleteQueued(id));
    } else if (line.hasOption(Conf.OPTION_KILL)) {
      String jsonPath = line.getOptionValue(Conf.OPTION_KILL);

      String json =
          StringUtils.join(
              FileUtils.readLines(new File(jsonPath), CharEncoding.UTF_8), Symbols.NEWLINE);

      JsonParser jsonParser = new JsonParser();

      JsonObject jsonObject = jsonParser.parse(json).getAsJsonObject();

      String name = jsonObject.getAsJsonPrimitive("name").getAsString();
      String queue = jsonObject.getAsJsonPrimitive("queue").getAsString();

      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

      Date scheduleTime = format.parse(jsonObject.getAsJsonPrimitive("scheduleTime").getAsString());

      ApplicationRecord record = client.getRecord(name, queue, scheduleTime);

      boolean flag = false;

      if (Objects.nonNull(record) && record.getState().equals(ApplicationState.RUNNING)) {
        String host = record.getHost();
        int port = Integer.valueOf(PROPERTIES.getString("server.port"));

        CosmosClient targetClient = new CosmosClient(host, port);

        if (targetClient.kill(name, queue, scheduleTime)) {
          flag = true;
        }
      }

      System.out.println("killed " + flag);
    } else if (line.hasOption(Conf.OPTION_LOG)) {
      String jsonPath = line.getOptionValue(Conf.OPTION_LOG);

      String json =
          StringUtils.join(
              FileUtils.readLines(new File(jsonPath), CharEncoding.UTF_8), Symbols.NEWLINE);

      JsonParser jsonParser = new JsonParser();

      JsonObject jsonObject = jsonParser.parse(json).getAsJsonObject();

      String name = jsonObject.getAsJsonPrimitive("name").getAsString();
      String queue = jsonObject.getAsJsonPrimitive("queue").getAsString();
      String scheduleTime = jsonObject.getAsJsonPrimitive("scheduleTime").getAsString();

      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

      ApplicationRecord record = client.getRecord(name, queue, format.parse(scheduleTime));
      if (Objects.nonNull(record)) {
        String host = record.getHost();
        int port = Integer.valueOf(PROPERTIES.getString("server.port"));

        CosmosClient targetClient = new CosmosClient(host, port);

        System.out.println(targetClient.log(name, queue, format.parse(scheduleTime)));
      }
    } else {
      formatter.printHelp("COMMAND", options);
    }
  }
}
