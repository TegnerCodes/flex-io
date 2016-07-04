package com.tegnercodes.flexio.pluginsystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.tegnercodes.flexio.pluginsystem.util.DirectedGraph;

public class DependencyResolver {

    private List<PluginWrapper> plugins;
    private DirectedGraph<String> dependenciesGraph;
    private DirectedGraph<String> dependentsGraph;
    private boolean resolved;

	public void resolve(List<PluginWrapper> plugins) {
		this.plugins = plugins;

        initGraph();

        resolved = true;
	}

    public List<String> getDependecies(String pluginsId) {
        if (!resolved) {
            return Collections.emptyList();
        }

        return dependenciesGraph.getNeighbors(pluginsId);
    }

    public List<String> getDependents(String pluginsId) {
        if (!resolved) {
            return Collections.emptyList();
        }

        return dependentsGraph.getNeighbors(pluginsId);
    }

	/**
	 * Get the list of plugins in dependency sorted order.
	 */
	public List<PluginWrapper> getSortedPlugins() throws PluginException {
        if (!resolved) {
            return Collections.emptyList();
        }

		List<String> pluginsId = dependenciesGraph.reverseTopologicalSort();

		if (pluginsId == null) {
			throw new CyclicDependencyException("Cyclic dependencies !!!" + dependenciesGraph.toString());
		}

		List<PluginWrapper> sortedPlugins = new ArrayList<>();
		for (String pluginId : pluginsId) {
			sortedPlugins.add(getPlugin(pluginId));
		}

		return sortedPlugins;
	}

    private void initGraph() {
        // create graph
        dependenciesGraph = new DirectedGraph<>();
        dependentsGraph = new DirectedGraph<>();

        // populate graph
        for (PluginWrapper pluginWrapper : plugins) {
            PluginDescriptor descriptor = pluginWrapper.getDescriptor();
            String pluginId = descriptor.getPluginId();
            List<PluginDependency> dependencies = descriptor.getDependencies();
            if (!dependencies.isEmpty()) {
                for (PluginDependency dependency : dependencies) {
                    dependenciesGraph.addEdge(pluginId, dependency.getPluginId());
                    dependentsGraph.addEdge(dependency.getPluginId(), pluginId);
                }
            } else {
                dependenciesGraph.addVertex(pluginId);
                dependentsGraph.addVertex(pluginId);
            }
        }
    }

    private PluginWrapper getPlugin(String pluginId) throws PluginNotFoundException {
		for (PluginWrapper pluginWrapper : plugins) {
			if (pluginId.equals(pluginWrapper.getDescriptor().getPluginId())) {
				return pluginWrapper;
			}
		}

		throw new PluginNotFoundException(pluginId);
	}

}
