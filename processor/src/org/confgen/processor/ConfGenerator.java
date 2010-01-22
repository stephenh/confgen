package org.confgen.processor;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Generated;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.JavaFileObject;
import javax.tools.Diagnostic.Kind;

import joist.sourcegen.GClass;
import joist.sourcegen.GMethod;

import org.confgen.Conf;

public class ConfGenerator {

	private final ProcessingEnvironment processingEnv;
	private final TypeElement element;
	private final GClass baseClass;
	private final GMethod cstr;

	public ConfGenerator(ProcessingEnvironment processingEnv, TypeElement element) {
		this.processingEnv = processingEnv;
		this.element = element;

		this.baseClass = new GClass(this.element.toString() + "Base");
		this.baseClass.getField("config").type("Config").setFinal().setProtected();
		this.baseClass.addImports("zjoist.util.Config".replace("z", "")); // z to fool jarjar
		this.cstr = this.baseClass.getConstructor();
		this.cstr.body.line("this.config = new joist.util.Config();");

		String date = new SimpleDateFormat("yyyy MMM dd hh:mm").format(new Date());
		this.baseClass.addImports(Generated.class).addAnnotation("@Generated(value = \"" + Processor.class.getName() + "\", date = \"" + date + "\")");
	}

	public void generate() {
		for (ExecutableElement method : ElementFilter.methodsIn(this.processingEnv.getElementUtils().getAllMembers(this.element))) {
			if (method.getModifiers().contains(Modifier.ABSTRACT)) {
				Conf conf = method.getAnnotation(Conf.class);
				if (conf != null) {
					generate(method, conf);
				}
			}
		}
		this.saveCode();
	}

	private void generate(ExecutableElement method, Conf conf) {
		String name = method.getSimpleName().toString();

		GMethod m = this.baseClass.getMethod(name);
		m.returnType(method.getReturnType().toString());
		m.body.line("return this.config.get(\"{}\");", conf.key());

		addUnlessSet(conf.key(), "local", conf.local());
		addUnlessSet(conf.key(), "dev", conf.dev());
		addUnlessSet(conf.key(), "qa", conf.qa());
		addUnlessSet(conf.key(), "prod", conf.prod());
	}

	private void addUnlessSet(String key, String stage, String value) {
		if (!"".equals(value)) {
			cstr.body.line("this.config.putUnlessSet(\"{}.{}\", \"{}\");", key, stage, value);
		}
	}

	private void saveCode() {
		try {
			JavaFileObject jfo = this.processingEnv.getFiler().createSourceFile(this.baseClass.getFullClassNameWithoutGeneric(), this.element);
			Writer w = jfo.openWriter();
			w.write(this.baseClass.toCode());
			w.close();
		} catch (IOException io) {
			this.processingEnv.getMessager().printMessage(Kind.ERROR, io.getMessage());
		}
	}

}
