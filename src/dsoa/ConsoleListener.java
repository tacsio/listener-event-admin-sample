package dsoa;

import java.util.Hashtable;
import java.util.Map;

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
	
	public ConsoleListener(BundleContext ctx) {
		this.ctx = ctx;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void listen() {
		listenInvocationEvent();
		listenAvgResponseTimeEvent();
		listenRequestOutOfScheduleEvent();
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
				System.out.println("["+ now() +"] " + "InvocationEvent("+ numInvocationEvent +"): " + dsoaEventMap.get("data_operationName") + " " + (res-req) + "ms");
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
				System.out.println("["+ now() +"] " +"AvgResponseTime("+ numAvgResponseTimeEvent +"): " + dsoaEventMap.get("data_value")+"ms");
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
				System.out.println("["+ now() +"] " +"RequestOutOfScheduleEvent("+ numRequestOutOfScheduleEvent +") EXCEPTION !" );
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
