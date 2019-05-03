package smc;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

public class ExecuteCommand implements Callable<String> {

    private String command;
    private SMCModel model;
    private String result;

    public ExecuteCommand(String cmd) {
        this.command = cmd;
    }

    public ExecuteCommand(String cmd, SMCModel model) {
        this.command = cmd;
        this.model = model;
    }

    @Override
    public String call() throws Exception {
        StringBuffer output = new StringBuffer();

        Process p;
        try {

            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            String line = "";
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }

            if (output.length() == 0) {
                while ((line = errorReader.readLine()) != null) {
                    output.append(line + "\n");
                }
            }

        } catch (Exception e) {
            e.printStackTrace(System.out);
        }

        result = output.toString();
        return result;
    }

    public SMCModel getModel() {
        return model;
    }

    public String getResult() {
        return result;
    }
}
