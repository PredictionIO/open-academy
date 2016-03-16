
import scipy.special as sci
import matplotlib.pyplot as plt
from matplotlib.pylab import *
from numpy import *
from math import *
import numpy as np
from numpy.random import normal, uniform, gamma


matplotlib.rcParams.update({'font.size': 12,
	'font.sans-serif' : 'Avant Garde'})

# Generates samples of size, sampleSize, from a 2-parameter gamma distribution, and
# plots a probability histogram of each sample & the pdf
def plot_gamma_hist(alpha, beta, color, fitLineColor, linewidth, sampleSize, numBins):

	shape, scale = alpha, beta # shape = k (aka mean) and scale = theta (aka dispersion)
	s = np.random.gamma(shape, scale, sampleSize)

	count, bins, ignored = plt.hist(s, numBins, normed=True)
	y = bins**(shape-1)*(np.exp(-bins/scale) /(sci.gamma(shape)*scale**shape))
	plt.plot(bins, y, linewidth=linewidth, color=fitLineColor)
	plt.xlabel('x')
	plt.ylabel('p(x|%s, %s)'% (alpha, beta))
	title("Gamma%s Sample of Size %s" % ((alpha, beta), sampleSize))
	plt.show()
	return

