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
	
	public ConsoleListener(BundleContext ctx) {
		this.ctx = ctx;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void listen() {
		listenAvgResponseTimeEvent();
		listenRequestOutOfScheduleEvent();
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
	
	private void listenAvgResponseTimeEvent() {
		final String topic = "AvgResponseTimeEvent";
		Hashtable props = new Hashtable();
		props.put(EventConstants.EVENT_TOPIC, new String[] { topic+"/*" });
		ctx.registerService(EventHandler.class.getName(), new EventHandler() {

			@Override
			public void handleEvent(org.osgi.service.event.Event event) {
				
				Map<String, Object> dsoaEventMap = (Map<String, Object>) event.getProperty(topic);
				System.out.println("AvgResponseTime: " + dsoaEventMap.get("data_value"));
				//parseMap(dsoaEventMap);
				numAvgResponseTimeEvent++;
				System.out.println(numAvgResponseTimeEvent);
			}
		}, props);
	}
	
	private void listenRequestOutOfScheduleEvent() {
		final String topic = "RequestOutOfScheduleEvent";
		Hashtable props = new Hashtable();
		props.put(EventConstants.EVENT_TOPIC, new String[] { topic+"/*" });
		ctx.registerService(EventHandler.class.getName(), new EventHandler() {

			@Override
			public void handleEvent(org.osgi.service.event.Event event) {
				
				Map<String, Object> dsoaEventMap = (Map<String, Object>) event.getProperty(topic);
				System.out.println("Exception: " + dsoaEventMap.get("metadata_timestamp"));
				//parseMap(dsoaEventMap);
				numRequestOutOfScheduleEvent++;
				System.out.println(numRequestOutOfScheduleEvent);
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
}
