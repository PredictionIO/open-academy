from plot_gamma_hist import *
from plot_normal_hist import plot_normal_hist
from colors import colors


sampleSizes = [20, 50, 100, 500]
numBins = [3, 11, 20, 77]

# print plots
def show_gamma_hist():
	for i in range(len(sampleSizes)-1):
		gamma_plot = plot_gamma_hist(7.5, 10, colors["ocean_blue"], colors["magenta"], 3, sampleSizes[i], numBins[i])
	return

def show_normal_hist():
	for i in range(len(sampleSizes)-1):
		norm_plot = plot_normal_hist(0, 1, colors["light_purple"], colors["forest_green"], 3, sampleSizes[i], numBins[i])
	return

show_normal_hist()