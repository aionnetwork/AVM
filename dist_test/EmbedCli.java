import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;


/**
 * A test provided to verify issue-331:  We correctly operate whether in the system classloader or another one.
 * This also demonstrates that the example and tools in the "dist" work correctly.
 */
public class EmbedCli {
    public static void main(String[] args) throws Exception {
        if (2 != args.length) {
            System.err.println("Usage:  EmbedCli <avm.jar> <dapp.jar>");
            System.exit(1);
        }
        
        String pathToAvm = args[0];
        String pathToDApp = args[1];
        deployEmbedded(pathToAvm, pathToDApp);
    }

    private static void deployEmbedded(String pathToAvm, String pathToDApp) throws Exception {
        URL avmJarUrl = new File(pathToAvm).toURI().toURL();
        
        // Create our classloader which should be able to safely run the AVM's CLI entry-point and deploy without issue.
        URLClassLoader classLoader = new URLClassLoader(new URL[]{ avmJarUrl });
        
        // Resolve the main().
        Class<?> clazz = classLoader.loadClass("org.aion.cli.AvmCLI");
        Method method = clazz.getMethod("main", String[].class);
        
        // Run the deployment as though we were invoking AvmCLI from the command-line.
        Object[] args = new Object[] { new String[] { "deploy", pathToDApp } };
        method.invoke(null, args);
        
        classLoader.close();
    }
}
