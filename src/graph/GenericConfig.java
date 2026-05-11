package graph;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/** Builds a graph of ParallelAgents from a plain-text config file (3 lines per agent). */
public class GenericConfig implements Config {

    private String confFile;
    private List<ParallelAgent> agents = new ArrayList<>();

    public void setConfFile(String path) {
        this.confFile = path;
    }

    @Override
    public void create() {
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(confFile));
        } catch (IOException e) {
            System.out.println("GenericConfig: cannot read file: " + confFile);
            return;
        }

        if (lines.size() % 3 != 0) {
            System.out.println("GenericConfig: file line count is not a multiple of 3");
            return;
        }

        for (int i = 0; i < lines.size(); i += 3) {
            String className = lines.get(i).trim();
            String[] subs = lines.get(i + 1).trim().split(",");
            String[] pubs = lines.get(i + 2).trim().split(",");

            try {
                Class<?> cls = Class.forName(className);
                Constructor<?> ctor = cls.getConstructor(String[].class, String[].class);
                Agent agent = (Agent) ctor.newInstance(new Object[]{subs, pubs});
                agents.add(new ParallelAgent(agent, 10));
            } catch (Exception e) {
                System.out.println("GenericConfig: failed to create agent " + className + ": " + e);
            }
        }
    }

    @Override
    public void close() {
        for (ParallelAgent pa : agents) {
            pa.close();
        }
    }

    @Override
    public String getName() { return "Generic Config"; }

    @Override
    public int getVersion() { return 1; }
}
