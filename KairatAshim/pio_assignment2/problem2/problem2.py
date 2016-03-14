import matplotlib.pyplot as plt
import numpy as np
import matplotlib.mlab as mlab
import math
import scipy.special as sps

mean = 0
variance = 1

sigma = math.sqrt(variance)

def drawSampleNormal(sampleSize):
    samples = np.random.normal(mean, sigma, sampleSize)
    count, bins, ignored = plt.hist(samples, 80, normed=True)
    plt.plot(bins,mlab.normpdf(bins,mean,sigma))
    plt.show()
    plt.savefig("normal_" + str(sampleSize) + "_samples.png")   
    plt.clf()

drawSampleNormal(20) 
drawSampleNormal(50)    
drawSampleNormal(100)    
drawSampleNormal(500)  

alpha = 7.5
beta = 10

def drawSampleGamma(sampleSize):
    samples = np.random.gamma(alpha, beta, sampleSize)
    count, bins, ignored = plt.hist(samples, 80, normed=True)
    pdf = bins**(alpha-1)*(np.exp(-bins/beta) / (sps.gamma(alpha)*beta**alpha))
    plt.plot(bins, pdf, linewidth=2, color='r')
    plt.show()
    plt.savefig("gamma_" + str(sampleSize) + "_samples.png")
    plt.clf()

drawSampleGamma(20)
drawSampleGamma(50)
drawSampleGamma(100)    
drawSampleGamma(500)