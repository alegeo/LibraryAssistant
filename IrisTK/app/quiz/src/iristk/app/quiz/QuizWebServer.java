package iristk.app.quiz;

import iristk.flow.FlowModule;
import iristk.system.Event;
import iristk.system.IrisUtils;
import iristk.util.Record;
import iristk.util.Utils;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class QuizWebServer extends AbstractHandler implements Runnable {

	private QuizFlow quizFlow;
	private Thread thread;
	private FlowModule flowModule;

	public QuizWebServer(FlowModule flowModule, QuizFlow flow) {
		this.quizFlow = flow;
		this.flowModule = flowModule;
		thread = new Thread(this);
		thread.start();
	}

	@Override
	public void handle(String target,
            Request baseRequest,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
		if (target.equals("/")) {
			response.setContentType("text/html;charset=utf-8");
	        response.setStatus(HttpServletResponse.SC_OK);
	        baseRequest.setHandled(true);
	        //java.util.Scanner s = new java.util.Scanner(this.getClass().getResourceAsStream("question.html")).useDelimiter("\\A");
	        //response.getWriter().print(s.hasNext() ? s.next() : "");
	        response.getWriter().print(Utils.readTextFile(new File(new File(IrisUtils.getPackagePath("quiz"), "web"), "question.html")));
		} else if (target.equals("/update")) {
			response.setContentType("application/json;charset=utf-8");
	        response.setStatus(HttpServletResponse.SC_OK);
	        baseRequest.setHandled(true);
	        Record update = new Record();
	        if (quizFlow.question != null) {
	        	update.putAll(quizFlow.question);
			} else {
				update.put("question", "");
				update.put("answer1", "");
				update.put("answer2", "");
				update.put("answer3", "");
				update.put("answer4", "");
			}
	        update.put("score1", quizFlow.users.getString("user-1:score", "0"));
	        update.put("score2", quizFlow.users.getString("user-2:score", "0"));
	        response.getWriter().print(update.toJSON());
		} else if (target.startsWith("/resource/")) {
			response.setContentType("application/javascript;charset=utf-8");
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);
			response.getWriter().print(Utils.readTextFile(new File(new File(IrisUtils.getPackagePath("quiz"), "web"), target.replace("/resource/", ""))));
		} else if (target.startsWith("/choose")) {
			String answer = target.split("_")[1];
			Event event = new Event("quiz.answer");
			event.put("answer", answer);
			System.out.println(answer);
			flowModule.invokeEvent(event); 
		} else {
	        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}

	@Override
	public void run() {
		Server server = new Server(8080);
        server.setHandler(this);
        try {
			server.start();
	        server.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
