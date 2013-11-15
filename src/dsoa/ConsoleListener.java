package dsoa;

import java.util.Hashtable;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

public class ConsoleListener {
	
	private BundleContext ctx;
	
	public ConsoleListener(BundleContext ctx) {
		this.ctx = ctx;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void listen() {
		
		final String topic = "IntrospectSampleEvent";
		Hashtable props = new Hashtable();
		props.put(EventConstants.EVENT_TOPIC, new String[] { topic });
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
				System.out.println(String.format("%s :: %s", key, element));
			}
		}
	}
}
