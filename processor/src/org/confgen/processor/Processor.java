package org.confgen.processor;

import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import javax.tools.Diagnostic.Kind;

import org.confgen.GenConf;

@SupportedAnnotationTypes( { "org.confgen.GenConf" })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class Processor extends AbstractProcessor {

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		try {
			for (TypeElement element : ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(GenConf.class))) {
				new ConfGenerator(this.processingEnv, (TypeElement) element).generate();
			}
		} catch (Exception e) {
			logExceptionToTextFile(e);
		}
		return true;
	}

	/** Logs <code>e</code> to <code>SOURCE_OUTPUT/confgen-errors.txt</code> */
	private void logExceptionToTextFile(Exception e) {
		try {
			FileObject fo = this.processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, "", "confgen-exception.txt");
			OutputStream out = fo.openOutputStream();
			e.printStackTrace(new PrintStream(out));
			// Specifically for Eclipse's AbortCompilation exception which has a useless printStackTrace output
			try {
				Field f = e.getClass().getField("problem");
				Object problem = f.get(e);
				out.write(problem.toString().getBytes());
			} catch (NoSuchFieldException nsfe) {
			}
			out.close();
		} catch (Exception e2) {
			this.processingEnv.getMessager().printMessage(Kind.ERROR, "Error writing out error message " + e2.getMessage());
		}
	}

}
