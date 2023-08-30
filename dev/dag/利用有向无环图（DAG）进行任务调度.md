```java
package cn.zz.bi.dag;

public interface Executor {
    boolean execute();
}
```

```java
package cn.zz.bi.dag;

public class Task implements Executor{
    private Long id;
    private String name;
    private int state;
    public Task(Long id, String name, int state) {
        this.id = id;
        this.name = name;
        this.state = state;
    }
    public boolean execute() {
        System.out.println("Task id: [" + id + "], " + "task name: [" + name +"] is running");
        state = 1;
        return true;
    }
    public boolean hasExecuted() {
        return state == 1;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getState() {
        return state;
    }
}

```

```java
package cn.zz.bi.dag;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * 这个类使用了邻接表来表示有向无环图。
 *
 * tasks是顶点集合，也就是任务集合。
 *
 * map是任务依赖关系集合。key是一个任务，value是它的前置任务集合。
 *
 * 一个任务执行的前提是它在map中没有以它作为key的entry，或者是它的前置任务集合中的任务都是已执行的状态。
 */
public class Digraph {
    private Set<Task> tasks;
    private Map<Task, Set<Task>> map;
    public Digraph() {
        this.tasks = new HashSet<Task>();
        this.map = new HashMap<Task, Set<Task>>();
    }
    public void addEdge(Task task, Task prev) {
        if (!tasks.contains(task) || !tasks.contains(prev)) {
            throw new IllegalArgumentException();
        }
        Set<Task> prevs = map.get(task);
        if (prevs == null) {
            prevs = new HashSet<Task>();
            map.put(task, prevs);
        }
        if (prevs.contains(prev)) {
            throw new IllegalArgumentException();
        }
        prevs.add(prev);
    }
    public void addTask(Task task) {
        if (tasks.contains(task)) {
            throw new IllegalArgumentException();
        }
        tasks.add(task);
    }
    public void remove(Task task) {
        if (!tasks.contains(task)) {
            return;
        }
        if (map.containsKey(task)) {
            map.remove(task);
        }
        for (Set<Task> set : map.values()) {
            if (set.contains(task)) {
                set.remove(task);
            }
        }
    }
    public Set<Task> getTasks() {
        return tasks;
    }
    public void setTasks(Set<Task> tasks) {
        this.tasks = tasks;
    }
    public Map<Task, Set<Task>> getMap() {
        return map;
    }
    public void setMap(Map<Task, Set<Task>> map) {
        this.map = map;
    }
}
```

```java
package cn.zz.bi.dag;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 调度器的实现比较简单，就是遍历任务集合，找出待执行的任务集合，放到一个List中，再串行执行
 * （若考虑性能，可优化为并行执行）。
 *
 * 若List为空，说明所有任务都已执行，则这一次任务调度结束。
 *
 * 一个任务执行的前提是它在map中没有以它作为key的entry，或者是它的前置任务集合中的任务都是已执行的状态。
 */
public class Scheduler {
    public void schedule(Digraph digraph) {
        while (true) {
            List<Task> todo = new ArrayList<Task>();
            System.out.println();
            for (Task task : digraph.getTasks()) {
                if (!task.hasExecuted()) {
                    Set<Task> prevs = digraph.getMap().get(task);
                    if (prevs != null && !prevs.isEmpty()) {
                        // 或者是它的前置任务集合中的任务都是已执行的状态
                        boolean toAdd = true;
                        for (Task task1 : prevs) {
                            if (!task1.hasExecuted()) {
                                toAdd = false;
                                break;
                            }
                        }
                        if (toAdd) {
                            todo.add(task);
                            String log = String.format("%s需要被执行，因为其前置任务[%s]都已经执行成功！！！\n",
                                    task.getName(), prevs.stream().map(Task::getName).collect(Collectors.toList()));
                            System.out.printf(log);
                        }
                    } else {
                        // 一个任务执行的前提是它在map中没有以它作为key的entry
                        todo.add(task);
                        String log = String.format("%s需要被执行，因为他是DAG开始执行的起点\n", task.getName());
                        System.out.printf(log);
                    }
                }
            }
            if (!todo.isEmpty()) {
                System.out.println("这些任务将被并行执行： " + todo.stream().map(Task::getName).collect(Collectors.toList()));
                // 这里可以优化为并行执行
                for (Task task : todo) {
                    if (!task.execute()) {
                        throw new RuntimeException();
                    }
                }
            } else {
                break;
            }
        }
    }

    public static void main(String[] args) {
        Digraph digraph = new Digraph();
        Task task1 = new Task(1L, "task1", 0);
        Task task2 = new Task(2L, "task2", 0);
        Task task3 = new Task(3L, "task3", 0);
        Task task4 = new Task(4L, "task4", 0);
        Task task5 = new Task(5L, "task5", 0);
        Task task6 = new Task(6L, "task6", 0);
        digraph.addTask(task1);
        digraph.addTask(task2);
        digraph.addTask(task3);
        digraph.addTask(task4);
        digraph.addTask(task5);
        digraph.addTask(task6);
        digraph.addEdge(task1, task2);
        digraph.addEdge(task1, task5);
        digraph.addEdge(task6, task2);
        digraph.addEdge(task2, task3);
        digraph.addEdge(task2, task4);
        Scheduler scheduler = new Scheduler();
        scheduler.schedule(digraph);
    }
}
```

```text
"C:\Program Files\Java\jdk1.8.0_191\bin\java.exe" -javaagent:D:\software\ideaIC-2022.3.1.win\lib\idea_rt.jar=63229:D:\software\ideaIC-2022.3.1.win\bin -Dfile.encoding=UTF-8 -classpath "C:\Program Files\Java\jdk1.8.0_191\jre\lib\charsets.jar;C:\Program Files\Java\jdk1.8.0_191\jre\lib\deploy.jar;C:\Program Files\Java\jdk1.8.0_191\jre\lib\ext\access-bridge-64.jar;C:\Program Files\Java\jdk1.8.0_191\jre\lib\ext\cldrdata.jar;C:\Program Files\Java\jdk1.8.0_191\jre\lib\ext\dnsns.jar;C:\Program Files\Java\jdk1.8.0_191\jre\lib\ext\jaccess.jar;C:\Program Files\Java\jdk1.8.0_191\jre\lib\ext\jfxrt.jar;C:\Program Files\Java\jdk1.8.0_191\jre\lib\ext\localedata.jar;C:\Program Files\Java\jdk1.8.0_191\jre\lib\ext\nashorn.jar;C:\Program Files\Java\jdk1.8.0_191\jre\lib\ext\sunec.jar;C:\Program Files\Java\jdk1.8.0_191\jre\lib\ext\sunjce_provider.jar;C:\Program Files\Java\jdk1.8.0_191\jre\lib\ext\sunmscapi.jar;C:\Program Files\Java\jdk1.8.0_191\jre\lib\ext\sunpkcs11.jar;C:\Program Files\Java\jdk1.8.0_191\jre\lib\ext\zipfs.jar;C:\Program Files\Java\jdk1.8.0_191\jre\lib\javaws.jar;C:\Program Files\Java\jdk1.8.0_191\jre\lib\jce.jar;C:\Program Files\Java\jdk1.8.0_191\jre\lib\jfr.jar;C:\Program Files\Java\jdk1.8.0_191\jre\lib\jfxswt.jar;C:\Program Files\Java\jdk1.8.0_191\jre\lib\jsse.jar;C:\Program Files\Java\jdk1.8.0_191\jre\lib\management-agent.jar;C:\Program Files\Java\jdk1.8.0_191\jre\lib\plugin.jar;C:\Program Files\Java\jdk1.8.0_191\jre\lib\resources.jar;C:\Program Files\Java\jdk1.8.0_191\jre\lib\rt.jar;D:\code\data-quality\target\classes;C:\Users\h2413005\.m2\repository\org\scala-lang\scala-library\2.12.15\scala-library-2.12.15.jar;C:\Users\h2413005\.m2\repository\junit\junit\4.4\junit-4.4.jar;C:\Users\h2413005\.m2\repository\com\thoughtworks\xstream\xstream\1.4.11\xstream-1.4.11.jar;C:\Users\h2413005\.m2\repository\xmlpull\xmlpull\1.1.3.1\xmlpull-1.1.3.1.jar;C:\Users\h2413005\.m2\repository\xpp3\xpp3_min\1.1.4c\xpp3_min-1.1.4c.jar;C:\Users\h2413005\.m2\repository\org\apache\spark\spark-core_2.12\3.2.3\spark-core_2.12-3.2.3.jar;C:\Users\h2413005\.m2\repository\org\apache\avro\avro\1.10.2\avro-1.10.2.jar;C:\Users\h2413005\.m2\repository\com\fasterxml\jackson\core\jackson-core\2.12.2\jackson-core-2.12.2.jar;C:\Users\h2413005\.m2\repository\org\apache\commons\commons-compress\1.20\commons-compress-1.20.jar;C:\Users\h2413005\.m2\repository\org\apache\avro\avro-mapred\1.10.2\avro-mapred-1.10.2.jar;C:\Users\h2413005\.m2\repository\org\apache\avro\avro-ipc\1.10.2\avro-ipc-1.10.2.jar;C:\Users\h2413005\.m2\repository\com\twitter\chill_2.12\0.10.0\chill_2.12-0.10.0.jar;C:\Users\h2413005\.m2\repository\com\esotericsoftware\kryo-shaded\4.0.2\kryo-shaded-4.0.2.jar;C:\Users\h2413005\.m2\repository\com\esotericsoftware\minlog\1.3.0\minlog-1.3.0.jar;C:\Users\h2413005\.m2\repository\org\objenesis\objenesis\2.5.1\objenesis-2.5.1.jar;C:\Users\h2413005\.m2\repository\com\twitter\chill-java\0.10.0\chill-java-0.10.0.jar;C:\Users\h2413005\.m2\repository\org\apache\xbean\xbean-asm9-shaded\4.20\xbean-asm9-shaded-4.20.jar;C:\Users\h2413005\.m2\repository\org\apache\hadoop\hadoop-client-api\3.3.1\hadoop-client-api-3.3.1.jar;C:\Users\h2413005\.m2\repository\org\apache\hadoop\hadoop-client-runtime\3.3.1\hadoop-client-runtime-3.3.1.jar;C:\Users\h2413005\.m2\repository\org\apache\htrace\htrace-core4\4.1.0-incubating\htrace-core4-4.1.0-incubating.jar;C:\Users\h2413005\.m2\repository\commons-logging\commons-logging\1.1.3\commons-logging-1.1.3.jar;C:\Users\h2413005\.m2\repository\org\apache\spark\spark-launcher_2.12\3.2.3\spark-launcher_2.12-3.2.3.jar;C:\Users\h2413005\.m2\repository\org\apache\spark\spark-kvstore_2.12\3.2.3\spark-kvstore_2.12-3.2.3.jar;C:\Users\h2413005\.m2\repository\org\fusesource\leveldbjni\leveldbjni-all\1.8\leveldbjni-all-1.8.jar;C:\Users\h2413005\.m2\repository\com\fasterxml\jackson\core\jackson-annotations\2.12.3\jackson-annotations-2.12.3.jar;C:\Users\h2413005\.m2\repository\org\apache\spark\spark-network-common_2.12\3.2.3\spark-network-common_2.12-3.2.3.jar;C:\Users\h2413005\.m2\repository\com\google\crypto\tink\tink\1.6.0\tink-1.6.0.jar;C:\Users\h2413005\.m2\repository\org\apache\spark\spark-network-shuffle_2.12\3.2.3\spark-network-shuffle_2.12-3.2.3.jar;C:\Users\h2413005\.m2\repository\org\apache\spark\spark-unsafe_2.12\3.2.3\spark-unsafe_2.12-3.2.3.jar;C:\Users\h2413005\.m2\repository\javax\activation\activation\1.1.1\activation-1.1.1.jar;C:\Users\h2413005\.m2\repository\org\apache\curator\curator-recipes\2.13.0\curator-recipes-2.13.0.jar;C:\Users\h2413005\.m2\repository\org\apache\curator\curator-framework\2.13.0\curator-framework-2.13.0.jar;C:\Users\h2413005\.m2\repository\org\apache\curator\curator-client\2.13.0\curator-client-2.13.0.jar;C:\Users\h2413005\.m2\repository\org\apache\zookeeper\zookeeper\3.6.2\zookeeper-3.6.2.jar;C:\Users\h2413005\.m2\repository\commons-lang\commons-lang\2.6\commons-lang-2.6.jar;C:\Users\h2413005\.m2\repository\org\apache\zookeeper\zookeeper-jute\3.6.2\zookeeper-jute-3.6.2.jar;C:\Users\h2413005\.m2\repository\org\apache\yetus\audience-annotations\0.5.0\audience-annotations-0.5.0.jar;C:\Users\h2413005\.m2\repository\jakarta\servlet\jakarta.servlet-api\4.0.3\jakarta.servlet-api-4.0.3.jar;C:\Users\h2413005\.m2\repository\commons-codec\commons-codec\1.15\commons-codec-1.15.jar;C:\Users\h2413005\.m2\repository\org\apache\commons\commons-lang3\3.12.0\commons-lang3-3.12.0.jar;C:\Users\h2413005\.m2\repository\org\apache\commons\commons-math3\3.4.1\commons-math3-3.4.1.jar;C:\Users\h2413005\.m2\repository\org\apache\commons\commons-text\1.10.0\commons-text-1.10.0.jar;C:\Users\h2413005\.m2\repository\commons-io\commons-io\2.8.0\commons-io-2.8.0.jar;C:\Users\h2413005\.m2\repository\commons-collections\commons-collections\3.2.2\commons-collections-3.2.2.jar;C:\Users\h2413005\.m2\repository\com\google\code\findbugs\jsr305\3.0.0\jsr305-3.0.0.jar;C:\Users\h2413005\.m2\repository\org\slf4j\slf4j-api\1.7.30\slf4j-api-1.7.30.jar;C:\Users\h2413005\.m2\repository\org\slf4j\jul-to-slf4j\1.7.30\jul-to-slf4j-1.7.30.jar;C:\Users\h2413005\.m2\repository\org\slf4j\jcl-over-slf4j\1.7.30\jcl-over-slf4j-1.7.30.jar;C:\Users\h2413005\.m2\repository\log4j\log4j\1.2.17\log4j-1.2.17.jar;C:\Users\h2413005\.m2\repository\org\slf4j\slf4j-log4j12\1.7.30\slf4j-log4j12-1.7.30.jar;C:\Users\h2413005\.m2\repository\com\ning\compress-lzf\1.0.3\compress-lzf-1.0.3.jar;C:\Users\h2413005\.m2\repository\org\xerial\snappy\snappy-java\1.1.8.4\snappy-java-1.1.8.4.jar;C:\Users\h2413005\.m2\repository\org\lz4\lz4-java\1.7.1\lz4-java-1.7.1.jar;C:\Users\h2413005\.m2\repository\com\github\luben\zstd-jni\1.5.0-4\zstd-jni-1.5.0-4.jar;C:\Users\h2413005\.m2\repository\org\roaringbitmap\RoaringBitmap\0.9.0\RoaringBitmap-0.9.0.jar;C:\Users\h2413005\.m2\repository\org\roaringbitmap\shims\0.9.0\shims-0.9.0.jar;C:\Users\h2413005\.m2\repository\commons-net\commons-net\3.1\commons-net-3.1.jar;C:\Users\h2413005\.m2\repository\org\scala-lang\modules\scala-xml_2.12\1.2.0\scala-xml_2.12-1.2.0.jar;C:\Users\h2413005\.m2\repository\org\scala-lang\scala-reflect\2.12.15\scala-reflect-2.12.15.jar;C:\Users\h2413005\.m2\repository\org\json4s\json4s-jackson_2.12\3.7.0-M11\json4s-jackson_2.12-3.7.0-M11.jar;C:\Users\h2413005\.m2\repository\org\json4s\json4s-core_2.12\3.7.0-M11\json4s-core_2.12-3.7.0-M11.jar;C:\Users\h2413005\.m2\repository\org\json4s\json4s-ast_2.12\3.7.0-M11\json4s-ast_2.12-3.7.0-M11.jar;C:\Users\h2413005\.m2\repository\org\json4s\json4s-scalap_2.12\3.7.0-M11\json4s-scalap_2.12-3.7.0-M11.jar;C:\Users\h2413005\.m2\repository\org\glassfish\jersey\core\jersey-client\2.34\jersey-client-2.34.jar;C:\Users\h2413005\.m2\repository\jakarta\ws\rs\jakarta.ws.rs-api\2.1.6\jakarta.ws.rs-api-2.1.6.jar;C:\Users\h2413005\.m2\repository\org\glassfish\hk2\external\jakarta.inject\2.6.1\jakarta.inject-2.6.1.jar;C:\Users\h2413005\.m2\repository\org\glassfish\jersey\core\jersey-common\2.34\jersey-common-2.34.jar;C:\Users\h2413005\.m2\repository\jakarta\annotation\jakarta.annotation-api\1.3.5\jakarta.annotation-api-1.3.5.jar;C:\Users\h2413005\.m2\repository\org\glassfish\hk2\osgi-resource-locator\1.0.3\osgi-resource-locator-1.0.3.jar;C:\Users\h2413005\.m2\repository\org\glassfish\jersey\core\jersey-server\2.34\jersey-server-2.34.jar;C:\Users\h2413005\.m2\repository\jakarta\validation\jakarta.validation-api\2.0.2\jakarta.validation-api-2.0.2.jar;C:\Users\h2413005\.m2\repository\org\glassfish\jersey\containers\jersey-container-servlet\2.34\jersey-container-servlet-2.34.jar;C:\Users\h2413005\.m2\repository\org\glassfish\jersey\containers\jersey-container-servlet-core\2.34\jersey-container-servlet-core-2.34.jar;C:\Users\h2413005\.m2\repository\org\glassfish\jersey\inject\jersey-hk2\2.34\jersey-hk2-2.34.jar;C:\Users\h2413005\.m2\repository\org\glassfish\hk2\hk2-locator\2.6.1\hk2-locator-2.6.1.jar;C:\Users\h2413005\.m2\repository\org\glassfish\hk2\external\aopalliance-repackaged\2.6.1\aopalliance-repackaged-2.6.1.jar;C:\Users\h2413005\.m2\repository\org\glassfish\hk2\hk2-api\2.6.1\hk2-api-2.6.1.jar;C:\Users\h2413005\.m2\repository\org\glassfish\hk2\hk2-utils\2.6.1\hk2-utils-2.6.1.jar;C:\Users\h2413005\.m2\repository\org\javassist\javassist\3.25.0-GA\javassist-3.25.0-GA.jar;C:\Users\h2413005\.m2\repository\io\netty\netty-all\4.1.68.Final\netty-all-4.1.68.Final.jar;C:\Users\h2413005\.m2\repository\com\clearspring\analytics\stream\2.9.6\stream-2.9.6.jar;C:\Users\h2413005\.m2\repository\io\dropwizard\metrics\metrics-core\4.2.0\metrics-core-4.2.0.jar;C:\Users\h2413005\.m2\repository\io\dropwizard\metrics\metrics-jvm\4.2.0\metrics-jvm-4.2.0.jar;C:\Users\h2413005\.m2\repository\io\dropwizard\metrics\metrics-json\4.2.0\metrics-json-4.2.0.jar;C:\Users\h2413005\.m2\repository\io\dropwizard\metrics\metrics-graphite\4.2.0\metrics-graphite-4.2.0.jar;C:\Users\h2413005\.m2\repository\io\dropwizard\metrics\metrics-jmx\4.2.0\metrics-jmx-4.2.0.jar;C:\Users\h2413005\.m2\repository\com\fasterxml\jackson\core\jackson-databind\2.12.3\jackson-databind-2.12.3.jar;C:\Users\h2413005\.m2\repository\com\fasterxml\jackson\module\jackson-module-scala_2.12\2.12.3\jackson-module-scala_2.12-2.12.3.jar;C:\Users\h2413005\.m2\repository\com\thoughtworks\paranamer\paranamer\2.8\paranamer-2.8.jar;C:\Users\h2413005\.m2\repository\org\apache\ivy\ivy\2.5.0\ivy-2.5.0.jar;C:\Users\h2413005\.m2\repository\oro\oro\2.0.8\oro-2.0.8.jar;C:\Users\h2413005\.m2\repository\net\razorvine\pyrolite\4.30\pyrolite-4.30.jar;C:\Users\h2413005\.m2\repository\net\sf\py4j\py4j\0.10.9.5\py4j-0.10.9.5.jar;C:\Users\h2413005\.m2\repository\org\apache\spark\spark-tags_2.12\3.2.3\spark-tags_2.12-3.2.3.jar;C:\Users\h2413005\.m2\repository\org\apache\commons\commons-crypto\1.1.0\commons-crypto-1.1.0.jar;C:\Users\h2413005\.m2\repository\org\spark-project\spark\unused\1.0.0\unused-1.0.0.jar;C:\Users\h2413005\.m2\repository\org\apache\spark\spark-sql_2.12\3.2.3\spark-sql_2.12-3.2.3.jar;C:\Users\h2413005\.m2\repository\org\rocksdb\rocksdbjni\6.20.3\rocksdbjni-6.20.3.jar;C:\Users\h2413005\.m2\repository\com\univocity\univocity-parsers\2.9.1\univocity-parsers-2.9.1.jar;C:\Users\h2413005\.m2\repository\org\apache\spark\spark-sketch_2.12\3.2.3\spark-sketch_2.12-3.2.3.jar;C:\Users\h2413005\.m2\repository\org\apache\spark\spark-catalyst_2.12\3.2.3\spark-catalyst_2.12-3.2.3.jar;C:\Users\h2413005\.m2\repository\org\scala-lang\modules\scala-parser-combinators_2.12\1.1.2\scala-parser-combinators_2.12-1.1.2.jar;C:\Users\h2413005\.m2\repository\org\codehaus\janino\janino\3.0.16\janino-3.0.16.jar;C:\Users\h2413005\.m2\repository\org\codehaus\janino\commons-compiler\3.0.16\commons-compiler-3.0.16.jar;C:\Users\h2413005\.m2\repository\javax\xml\bind\jaxb-api\2.2.11\jaxb-api-2.2.11.jar;C:\Users\h2413005\.m2\repository\org\apache\arrow\arrow-vector\2.0.0\arrow-vector-2.0.0.jar;C:\Users\h2413005\.m2\repository\org\apache\arrow\arrow-format\2.0.0\arrow-format-2.0.0.jar;C:\Users\h2413005\.m2\repository\org\apache\arrow\arrow-memory-core\2.0.0\arrow-memory-core-2.0.0.jar;C:\Users\h2413005\.m2\repository\com\google\flatbuffers\flatbuffers-java\1.9.0\flatbuffers-java-1.9.0.jar;C:\Users\h2413005\.m2\repository\org\apache\arrow\arrow-memory-netty\2.0.0\arrow-memory-netty-2.0.0.jar;C:\Users\h2413005\.m2\repository\org\apache\orc\orc-core\1.6.14\orc-core-1.6.14.jar;C:\Users\h2413005\.m2\repository\org\apache\orc\orc-shims\1.6.14\orc-shims-1.6.14.jar;C:\Users\h2413005\.m2\repository\com\google\protobuf\protobuf-java\2.5.0\protobuf-java-2.5.0.jar;C:\Users\h2413005\.m2\repository\io\airlift\aircompressor\0.21\aircompressor-0.21.jar;C:\Users\h2413005\.m2\repository\org\jetbrains\annotations\17.0.0\annotations-17.0.0.jar;C:\Users\h2413005\.m2\repository\org\threeten\threeten-extra\1.5.0\threeten-extra-1.5.0.jar;C:\Users\h2413005\.m2\repository\org\apache\orc\orc-mapreduce\1.6.14\orc-mapreduce-1.6.14.jar;C:\Users\h2413005\.m2\repository\org\apache\hive\hive-storage-api\2.7.2\hive-storage-api-2.7.2.jar;C:\Users\h2413005\.m2\repository\org\apache\parquet\parquet-column\1.12.2\parquet-column-1.12.2.jar;C:\Users\h2413005\.m2\repository\org\apache\parquet\parquet-common\1.12.2\parquet-common-1.12.2.jar;C:\Users\h2413005\.m2\repository\org\apache\parquet\parquet-encoding\1.12.2\parquet-encoding-1.12.2.jar;C:\Users\h2413005\.m2\repository\org\apache\parquet\parquet-hadoop\1.12.2\parquet-hadoop-1.12.2.jar;C:\Users\h2413005\.m2\repository\org\apache\parquet\parquet-format-structures\1.12.2\parquet-format-structures-1.12.2.jar;C:\Users\h2413005\.m2\repository\org\apache\parquet\parquet-jackson\1.12.2\parquet-jackson-1.12.2.jar;C:\Users\h2413005\.m2\repository\com\databricks\spark-xml_2.11\0.4.1\spark-xml_2.11-0.4.1.jar;C:\Users\h2413005\.m2\repository\org\apache\spark\spark-avro_2.12\3.2.3\spark-avro_2.12-3.2.3.jar;C:\Users\h2413005\.m2\repository\org\tukaani\xz\1.8\xz-1.8.jar;C:\Users\h2413005\.m2\repository\org\apache\spark\spark-hive_2.12\3.2.3\spark-hive_2.12-3.2.3.jar;C:\Users\h2413005\.m2\repository\org\apache\hive\hive-common\2.3.9\hive-common-2.3.9.jar;C:\Users\h2413005\.m2\repository\commons-cli\commons-cli\1.2\commons-cli-1.2.jar;C:\Users\h2413005\.m2\repository\jline\jline\2.12\jline-2.12.jar;C:\Users\h2413005\.m2\repository\com\tdunning\json\1.8\json-1.8.jar;C:\Users\h2413005\.m2\repository\com\github\joshelser\dropwizard-metrics-hadoop-metrics2-reporter\0.1.2\dropwizard-metrics-hadoop-metrics2-reporter-0.1.2.jar;C:\Users\h2413005\.m2\repository\org\apache\hive\hive-exec\2.3.9\hive-exec-2.3.9-core.jar;C:\Users\h2413005\.m2\repository\org\apache\hive\hive-vector-code-gen\2.3.9\hive-vector-code-gen-2.3.9.jar;C:\Users\h2413005\.m2\repository\org\apache\velocity\velocity\1.5\velocity-1.5.jar;C:\Users\h2413005\.m2\repository\org\antlr\antlr-runtime\3.5.2\antlr-runtime-3.5.2.jar;C:\Users\h2413005\.m2\repository\org\antlr\ST4\4.0.4\ST4-4.0.4.jar;C:\Users\h2413005\.m2\repository\com\google\code\gson\gson\2.2.4\gson-2.2.4.jar;C:\Users\h2413005\.m2\repository\stax\stax-api\1.0.1\stax-api-1.0.1.jar;C:\Users\h2413005\.m2\repository\org\apache\hive\hive-metastore\2.3.9\hive-metastore-2.3.9.jar;C:\Users\h2413005\.m2\repository\javolution\javolution\5.5.1\javolution-5.5.1.jar;C:\Users\h2413005\.m2\repository\com\jolbox\bonecp\0.8.0.RELEASE\bonecp-0.8.0.RELEASE.jar;C:\Users\h2413005\.m2\repository\com\zaxxer\HikariCP\2.5.1\HikariCP-2.5.1.jar;C:\Users\h2413005\.m2\repository\org\datanucleus\datanucleus-api-jdo\4.2.4\datanucleus-api-jdo-4.2.4.jar;C:\Users\h2413005\.m2\repository\org\datanucleus\datanucleus-rdbms\4.1.19\datanucleus-rdbms-4.1.19.jar;C:\Users\h2413005\.m2\repository\commons-pool\commons-pool\1.5.4\commons-pool-1.5.4.jar;C:\Users\h2413005\.m2\repository\commons-dbcp\commons-dbcp\1.4\commons-dbcp-1.4.jar;C:\Users\h2413005\.m2\repository\javax\jdo\jdo-api\3.0.1\jdo-api-3.0.1.jar;C:\Users\h2413005\.m2\repository\javax\transaction\jta\1.1\jta-1.1.jar;C:\Users\h2413005\.m2\repository\org\datanucleus\javax.jdo\3.2.0-m3\javax.jdo-3.2.0-m3.jar;C:\Users\h2413005\.m2\repository\javax\transaction\transaction-api\1.1\transaction-api-1.1.jar;C:\Users\h2413005\.m2\repository\org\apache\hive\hive-serde\2.3.9\hive-serde-2.3.9.jar;C:\Users\h2413005\.m2\repository\net\sf\opencsv\opencsv\2.3\opencsv-2.3.jar;C:\Users\h2413005\.m2\repository\org\apache\hive\hive-shims\2.3.9\hive-shims-2.3.9.jar;C:\Users\h2413005\.m2\repository\org\apache\hive\shims\hive-shims-common\2.3.9\hive-shims-common-2.3.9.jar;C:\Users\h2413005\.m2\repository\org\apache\hive\shims\hive-shims-0.23\2.3.9\hive-shims-0.23-2.3.9.jar;C:\Users\h2413005\.m2\repository\org\apache\hive\shims\hive-shims-scheduler\2.3.9\hive-shims-scheduler-2.3.9.jar;C:\Users\h2413005\.m2\repository\org\apache\hive\hive-llap-common\2.3.9\hive-llap-common-2.3.9.jar;C:\Users\h2413005\.m2\repository\org\apache\hive\hive-llap-client\2.3.9\hive-llap-client-2.3.9.jar;C:\Users\h2413005\.m2\repository\org\apache\httpcomponents\httpclient\4.5.13\httpclient-4.5.13.jar;C:\Users\h2413005\.m2\repository\org\apache\httpcomponents\httpcore\4.4.13\httpcore-4.4.13.jar;C:\Users\h2413005\.m2\repository\org\codehaus\jackson\jackson-mapper-asl\1.9.13\jackson-mapper-asl-1.9.13.jar;C:\Users\h2413005\.m2\repository\org\codehaus\jackson\jackson-core-asl\1.9.13\jackson-core-asl-1.9.13.jar;C:\Users\h2413005\.m2\repository\joda-time\joda-time\2.10.10\joda-time-2.10.10.jar;C:\Users\h2413005\.m2\repository\org\jodd\jodd-core\3.5.2\jodd-core-3.5.2.jar;C:\Users\h2413005\.m2\repository\org\datanucleus\datanucleus-core\4.1.17\datanucleus-core-4.1.17.jar;C:\Users\h2413005\.m2\repository\org\apache\thrift\libthrift\0.12.0\libthrift-0.12.0.jar;C:\Users\h2413005\.m2\repository\org\apache\thrift\libfb303\0.9.3\libfb303-0.9.3.jar;C:\Users\h2413005\.m2\repository\org\apache\derby\derby\10.14.2.0\derby-10.14.2.0.jar;C:\Users\h2413005\.m2\repository\com\amazon\deequ\deequ\2.0.1-spark-3.2\deequ-2.0.1-spark-3.2.jar;C:\Users\h2413005\.m2\repository\org\scalanlp\breeze_2.12\0.13.2\breeze_2.12-0.13.2.jar;C:\Users\h2413005\.m2\repository\org\scalanlp\breeze-macros_2.12\0.13.2\breeze-macros_2.12-0.13.2.jar;C:\Users\h2413005\.m2\repository\com\github\fommil\netlib\core\1.1.2\core-1.1.2.jar;C:\Users\h2413005\.m2\repository\net\sourceforge\f2j\arpack_combined_all\0.1\arpack_combined_all-0.1.jar;C:\Users\h2413005\.m2\repository\com\github\rwl\jtransforms\2.4.0\jtransforms-2.4.0.jar;C:\Users\h2413005\.m2\repository\org\spire-math\spire_2.12\0.13.0\spire_2.12-0.13.0.jar;C:\Users\h2413005\.m2\repository\org\spire-math\spire-macros_2.12\0.13.0\spire-macros_2.12-0.13.0.jar;C:\Users\h2413005\.m2\repository\org\typelevel\machinist_2.12\0.6.1\machinist_2.12-0.6.1.jar;C:\Users\h2413005\.m2\repository\com\chuusai\shapeless_2.12\2.3.2\shapeless_2.12-2.3.2.jar;C:\Users\h2413005\.m2\repository\org\typelevel\macro-compat_2.12\1.1.1\macro-compat_2.12-1.1.1.jar;C:\Users\h2413005\.m2\repository\com\alibaba\druid\1.2.15\druid-1.2.15.jar;C:\Users\h2413005\.m2\repository\org\antlr\antlr4-runtime\4.9.3\antlr4-runtime-4.9.3.jar;C:\Users\h2413005\.m2\repository\cn\hutool\hutool-all\5.8.20\hutool-all-5.8.20.jar;C:\Users\h2413005\.m2\repository\net\agkn\hll\1.6.0\hll-1.6.0.jar;C:\Users\h2413005\.m2\repository\it\unimi\dsi\fastutil\6.5.11\fastutil-6.5.11.jar;C:\Users\h2413005\.m2\repository\com\google\guava\guava\32.1.1-jre\guava-32.1.1-jre.jar;C:\Users\h2413005\.m2\repository\com\google\guava\failureaccess\1.0.1\failureaccess-1.0.1.jar;C:\Users\h2413005\.m2\repository\com\google\guava\listenablefuture\9999.0-empty-to-avoid-conflict-with-guava\listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar;C:\Users\h2413005\.m2\repository\org\checkerframework\checker-qual\3.33.0\checker-qual-3.33.0.jar;C:\Users\h2413005\.m2\repository\com\google\errorprone\error_prone_annotations\2.18.0\error_prone_annotations-2.18.0.jar;C:\Users\h2413005\.m2\repository\com\google\j2objc\j2objc-annotations\2.8\j2objc-annotations-2.8.jar" cn.zz.bi.dag.Scheduler

task3需要被执行，因为他是DAG开始执行的起点
task4需要被执行，因为他是DAG开始执行的起点
task5需要被执行，因为他是DAG开始执行的起点
这些任务将被并行执行： [task3, task4, task5]
Task id: [3], task name: [task3] is running
Task id: [4], task name: [task4] is running
Task id: [5], task name: [task5] is running

task2需要被执行，因为其前置任务[[task3, task4]]都已经执行成功！！！
这些任务将被并行执行： [task2]
Task id: [2], task name: [task2] is running

task6需要被执行，因为其前置任务[[task2]]都已经执行成功！！！
task1需要被执行，因为其前置任务[[task2, task5]]都已经执行成功！！！
这些任务将被并行执行： [task6, task1]
Task id: [6], task name: [task6] is running
Task id: [1], task name: [task1] is running


Process finished with exit code 0

```
