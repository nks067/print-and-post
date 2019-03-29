package print.and.post.camel.bridge;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.camel.CamelContext;
import org.apache.camel.component.vertx.VertxComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.RoutesDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;


import io.vertx.camel.CamelBridge;
import io.vertx.camel.CamelBridgeOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.ignite.IgniteClusterManager;
import print.and.post.camel.bridge.util.BridgeUtil;

/**
 * This class provides.
 *
 * 
 */
public class MosipBridgeFactory extends AbstractVerticle {

	/** The reg proc logger. */
	//private static Logger regProcLogger = RegProcessorLogger.getLogger(MosipBridgeFactory.class);
	private static final Logger regProcLogger = LoggerFactory.getLogger(MosipBridgeFactory.class);
	/**
	 * Gets the event bus.
	 *
	 * @return the event bus
	 */
	public static void getEventBus() {
		String configServerUri = BridgeUtil.getPropertyFromConfigServer("vertx.ignite.configuration");
		URL url = null;
		try {
			url = new URL(configServerUri);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		ClusterManager clusterManager = new IgniteClusterManager(url);
		VertxOptions options = new VertxOptions().setClusterManager(clusterManager).setHAEnabled(true)
				.setClustered(true);

		Vertx.clusteredVertx(options, vertx -> {
			if (vertx.succeeded()) {
				vertx.result().deployVerticle(MosipBridgeFactory.class.getName(),
						new DeploymentOptions().setHa(true).setWorker(true));
			} else {
				regProcLogger.error("failed : "+vertx.cause().toString());
			}
		});
	}

	@Override
	public void start() throws Exception {
		CamelContext camelContext = new DefaultCamelContext();
		VertxComponent vertxComponent = new VertxComponent();
		vertxComponent.setVertx(vertx);
		RestTemplate restTemplate = new RestTemplate();
		String url = BridgeUtil.getPropertyFromConfigServer("camel.routes.configuration");
		ResponseEntity<Resource> responseEntity = restTemplate.exchange(url, HttpMethod.GET, null, Resource.class);
		RoutesDefinition routes = camelContext.loadRoutesDefinition(responseEntity.getBody().getInputStream());
		camelContext.addRouteDefinitions(routes.getRoutes());
		camelContext.addComponent("vertx", vertxComponent);
		camelContext.start();
		CamelBridge.create(vertx, new CamelBridgeOptions(camelContext)).start();
	}
}
