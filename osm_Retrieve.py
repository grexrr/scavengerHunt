import osmnx as ox
import matplotlib.pyplot as plt

# Setup Range to retrieve data from OSM based on place name
place_name = "Cork, Ireland"
graph = ox.graph_from_place(place_name, network_type='drive')

# GEOCODE the origin and destination
origin = ox.geocode("Mallow, Cork, Ireland")
destination = ox.geocode("University College Cork, Cork, Ireland")

# find the nearest nodes to the origin and destination
origin_node = ox.nearest_nodes(graph, origin[1], origin[0])
destination_node = ox.nearest_nodes(graph, destination[1], destination[0])

# calculate the shortest path
route = ox.shortest_path(graph, origin_node, destination_node)

# plot the route
fig, ax = ox.plot_graph_route(graph,
                             route,
                             node_size=0, 
                             edge_color='#777777',
                             route_color='red',
                             route_linewidth=2,
                             show=False,
                             close=False)
plt.show()