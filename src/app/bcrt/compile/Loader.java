package app.bcrt.compile;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
class Loader {
    public static Val loadFile(String path, Val context) {
        String[] splitByDot = path.split("\\.");
        if(splitByDot.length >= 2) {
            String extension = splitByDot[splitByDot.length - 1];
            switch(extension) {
                case "bcrt":
                    return loadBcrtMethod(path, context);
                case "java":
                    return loadJavaMethod(path, context);
                default:
                    throw new IllegalArgumentException("File type not recognized");
            }
        } else throw new IllegalArgumentException("Input must be path to file");
    }

    public static Val loadBcrtMethod(String path, Val context) {
        Val ret = context.interpret("{" + StringTool.getCode(path) + "}");
        ret.holder = context.getVarHolder();
        return ret;
    }

    public static Val loadJavaMethod(String path, Val context) {
        File newMeth = new File(path);
        if(newMeth.getParentFile().exists() || newMeth.getParentFile().mkdirs()) {
            try {
                // Compilation Requirements
                DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
                JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
                StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

                Iterable<? extends JavaFileObject> compilationUnit = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(newMeth));
                JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnit);
                if(task.call()) {
                    // Load and execute
                    URLClassLoader classLoader = new URLClassLoader(new URL[] { newMeth.getParentFile().toURI().toURL() });
                    String name = newMeth.getName().replaceAll("\\..*$", ""); // remove file extension
                    Object newmethod = classLoader.loadClass(name).getConstructors()[0].newInstance();

                    classLoader.close();
                    if(newmethod instanceof Val){
                        Val result = (Val) newmethod;
                        result.holder = context.getVarHolder();
                        return result;
                    } else throw new IllegalArgumentException("Improper format");
                } else {
                    for(Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                        System.err.format("Error on line %d in %s%n", diagnostic.getLineNumber(), diagnostic.getSource().toUri());
                    }
                }
                fileManager.close();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException | SecurityException | IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}