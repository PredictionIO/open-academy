import matplotlib.pyplot as plt
from matplotlib.pylab import *
import numpy as np
from numpy import *
from math import *
from numpy.random import normal


# Font
matplotlib.rcParams.update({'font.size': 12,
	'font' : 'Avant Garde'})

# Generates samples of size, sampleSize, from a normal distribution, and
# plots a probability histogram of each sample & the pdf
def plot_normal_hist(alpha, beta, color, fitLineColor, linewidth, sampleSize, numBins):
	mu, sigma = alpha, beta # mean and standard deviation. usually mu, sigma = 0, 1

	sample = np.random.normal(mu, sigma, sampleSize)

	count, bins, ignored = plt.hist(sample, numBins, normed=True, color=color)
	plt.plot(bins, 1/(sigma * np.sqrt(2 * np.pi)) * np.exp( - (bins - mu)**2 / (2 * sigma**2) ), linewidth=2, color=fitLineColor)
	plt.xlabel('x')
	plt.ylabel('p(x|%s,%s)'% (alpha,beta))
	title("Normal%s Sample of Size %s" % ((alpha,beta), sampleSize))
	plt.show()
	return




