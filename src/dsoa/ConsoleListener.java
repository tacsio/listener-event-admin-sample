package dsoa;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Formatter;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import br.ufpe.cin.dsoa.util.Constants;

public class ConsoleListener {
	
	private BundleContext ctx;
	
	private int numAvgResponseTimeEvent = 0;
	private int numRequestOutOfScheduleEvent = 0;
	private int numInvocationEvent = 0;
	private long time = 0;
	
	
	private Logger invocation;
	private Logger avg;
	private Logger exception;
	
	private FileHandler invocation_f;
	private FileHandler avg_f;
	private FileHandler exception_f;
	
	
	public ConsoleListener(BundleContext ctx) {
		this.ctx = ctx;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void listen() {
		try {
			configureLogger();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		listenInvocationEvent();
		listenAvgResponseTimeEvent();
		listenRequestOutOfScheduleEvent();
	}
	
	private void configureLogger() throws SecurityException, IOException {

		java.util.logging.Formatter f = new java.util.logging.Formatter() {
			
			private final DateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");

			public String format(LogRecord record) {
				StringBuilder builder = new StringBuilder(1000);
				builder.append(formatMessage(record));
				builder.append("\n");
				return builder.toString();
			}
		};
		invocation = Logger.getLogger("Invocation");
		invocation_f = new FileHandler("log_invocation.txt");
		invocation_f.setFormatter(f);
		invocation.addHandler(invocation_f);
		
		
		avg = Logger.getLogger("AvgResponseTime");
		avg_f = new FileHandler("log_avg.txt");
		avg_f.setFormatter(f);
		avg.addHandler(avg_f);
		
		exception = Logger.getLogger("Exception");
		exception_f = new FileHandler("log_exception.txt");
		exception_f.setFormatter(f);
		exception.addHandler(exception_f);
		
	}

	private void listenInvocationEvent() {
		
		final String eventTypeName = "InvocationEvent";
		final String topic = String.format("%s/*",eventTypeName);

		@SuppressWarnings("rawtypes")
		Hashtable props = new Hashtable();
		props.put(EventConstants.EVENT_TOPIC, new String[] { topic });
		ctx.registerService(EventHandler.class.getName(), new EventHandler() {

			@Override
			public void handleEvent(org.osgi.service.event.Event event) {
				
				Map<String, Object> dsoaEventMap = (Map<String, Object>) event.getProperty(eventTypeName);
				long req = (Long) dsoaEventMap.get("data_requestTimestamp");
				long res = (Long) dsoaEventMap.get("data_responseTimestamp");
				numInvocationEvent++;
				invocation.log(Level.INFO, now() +"," +  (res-req));
				//parseMap(dsoaEventMap);
			}
		}, props);
	}
	
	private void listenAvgResponseTimeEvent() {
		final String topic = "AvgResponseTimeEvent";
		Hashtable props = new Hashtable();
		props.put(EventConstants.EVENT_TOPIC, new String[] { topic+"/*" });
		ctx.registerService(EventHandler.class.getName(), new EventHandler() {

			@Override
			public void handleEvent(org.osgi.service.event.Event event) {
				
				Map<String, Object> dsoaEventMap = (Map<String, Object>) event.getProperty(topic);
				//parseMap(dsoaEventMap);
				numAvgResponseTimeEvent++;
				avg.log(Level.INFO,now() + "," + dsoaEventMap.get("data_value"));
			}
		}, props);
	}
	
	private void listenRequestOutOfScheduleEvent() {

		final String eventTypeName = "RequestOutOfScheduleEvent";
		final String topic = String.format("%s/*",eventTypeName);

		@SuppressWarnings("rawtypes")
		Hashtable props = new Hashtable();
		props.put(EventConstants.EVENT_TOPIC, new String[] { topic });
		ctx.registerService(EventHandler.class.getName(), new EventHandler() {

			@Override
			public void handleEvent(org.osgi.service.event.Event event) {
				
				Map<String, Object> dsoaEventMap = (Map<String, Object>) event.getProperty(eventTypeName);
				numRequestOutOfScheduleEvent++;
				exception.log(Level.INFO, now() + "EXCEPTION");
				//parseMap(dsoaEventMap);
			}
		}, props);
	}

	private void listenBindEvent() {
		final String topic = Constants.BIND_EVENT;
		Hashtable props = new Hashtable();
		props.put(EventConstants.EVENT_TOPIC, new String[] { topic+"/*" });
		ctx.registerService(EventHandler.class.getName(), new EventHandler() {

			@Override
			public void handleEvent(org.osgi.service.event.Event event) {
				
				Map<String, Object> dsoaEventMap = (Map<String, Object>) event.getProperty(topic);
				parseMap(dsoaEventMap);
			}
		}, props);		
	}

	private void listenUnbindEvent() {
		final String topic = Constants.UNBIND_EVENT;
		Hashtable props = new Hashtable();
		props.put(EventConstants.EVENT_TOPIC, new String[] { topic+"/*" });
		ctx.registerService(EventHandler.class.getName(), new EventHandler() {

			@Override
			public void handleEvent(org.osgi.service.event.Event event) {
				
				Map<String, Object> dsoaEventMap = (Map<String, Object>) event.getProperty(topic);
				parseMap(dsoaEventMap);
			}
		}, props);		
	}

	private void listenIntrospectionEvent() {
		final String topic = "IntrospectSampleEvent";
		Hashtable props = new Hashtable();
		props.put(EventConstants.EVENT_TOPIC, new String[] { topic+"/*" });
		ctx.registerService(EventHandler.class.getName(), new EventHandler() {

			@Override
			public void handleEvent(org.osgi.service.event.Event event) {
				
				Map<String, Object> dsoaEventMap = (Map<String, Object>) event.getProperty(topic);
				parseMap(dsoaEventMap);
			}
		}, props);
	}
	
	@SuppressWarnings("unchecked")
	public final void parseMap(Map<String, Object> map){
		for(String key : map.keySet()){
			Object element = map.get(key);
			if(element instanceof Map){
				this.parseMap((Map<String, Object>) element);
			} else {
				System.out.println(String.format("EVENT: %s :: %s", key, element));
			}
		}
	}
	
	private String now(){
		
		if(time == 0){
			time = System.currentTimeMillis();
			return "0";
		}
		
		long current = System.currentTimeMillis();
		String now = (current - time)+"";

		return now;
	}
}
