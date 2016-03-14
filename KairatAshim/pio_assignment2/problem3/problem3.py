# -*- coding: utf-8 -*-
import matplotlib.pyplot as plt
import numpy as np
import matplotlib.mlab as mlab
import math
import scipy.special as sps
import scipy.stats as stats
from scipy.stats import invgamma

pathToFile = "/Users/kairat/Desktop/pio_assignment/positiveSixMonthRevenue.csv"

bins_value = 500

##################   3A   ##########################

def generateHistogram(path):
    f = open(path, 'r')
    data = []
    for line in f:
        data.append(float(line))
    
    plt.hist(data, bins_value, normed=True)
    plt.show()
    plt.savefig("positiveSixMonthRevenue-Histogram.png")
    plt.clf()

generateHistogram(pathToFile)




##################   3B   ##########################

############ Gamma Distribution ###############

'''
# estimate for k - https://en.wikipedia.org/wiki/Gamma_distribution#Parameter_estimation
def getMLEfor_k_approximate(path):
    f = open(path, 'r')
    data = []
    for line in f:
        data.append(float(line))
    
    N = len(data)
    
    s1 = sum(data)/N
    s1 = math.log(s1)
    
    s2 = 0;
    for val in data:
        s2 += math.log(val)
    
    s2 = s2/N
    
    s = s1 - s2
    
    k_approx = (3 - s + math.sqrt((s-3)**2 + 24*s))/(12*s)
    return k_approx
    
def getMLEfor_theta_approx(path):
    k = getMLEfor_k_approximate(path)
    
    f = open(path, 'r')
    data = []
    for line in f:
        data.append(float(line))
    
    N = len(data)
    
    theta_approx = sum(data)/(k*N)
    return theta_approx
    

def plotDataHistogramAndGammaPDF_approximate(path):
    alpha = getMLEfor_k_approximate(path)
    beta = getMLEfor_theta_approx(path)
    
    f = open(path, 'r')
    data = []
    for line in f:
        data.append(float(line))
        
    # plot histogram
    count, bins, ignored = plt.hist(data, 30, normed=True)
    
    # plot pdf
    gamma_pdf = bins**(alpha-1)*(np.exp(-bins/beta) / (sps.gamma(alpha)*beta**alpha))
    plt.plot(bins, gamma_pdf, linewidth=2, color='r')
    
    plt.show()
    plt.savefig("gamma_pdf_and_data_histogram.png")
    plt.clf()

'''
def plotDataHistogramAndGammaPDF(path):
    f = open(path, 'r')
    data = []
    for line in f:
        data.append(float(line))
    
    fit_alpha, fit_loc, fit_beta = stats.gamma.fit(data)
    print "MLE k = ", fit_alpha
    print "MLE theta = ", fit_beta
    
    # plot histogram
    count, bins, ignored = plt.hist(data, bins_value, normed=True)
    
    # plot pdf
    gamma_pdf = bins**(fit_alpha-1)*(np.exp(-bins/fit_beta) / (sps.gamma(fit_alpha)*fit_beta**fit_alpha))
    plt.plot(bins, gamma_pdf, linewidth=2, color='r')
    
    plt.show()
    plt.savefig("gamma_pdf_and_data_histogram.png")
    plt.clf()

    
plotDataHistogramAndGammaPDF(pathToFile)   


############ Inverse-Gamma Distribution ###############

def plotDataHistogramAndInverseGammaPDF(path):
    f = open(path, 'r')
    data = []
    for line in f:
        data.append(float(line))
    
    fit_alpha, fit_loc, fit_beta = stats.invgamma.fit(data)
    print "MLE k = ", fit_alpha
    print "MLE theta = ", fit_beta
    
    # plot histogram
    count, bins, ignored = plt.hist(data, bins_value, normed=True)
    
    # plot pdf
    inv_gamma_pdf = ((fit_beta**fit_alpha)*bins**(-fit_alpha-1)/sps.gamma(fit_alpha))*np.exp(-fit_beta/bins)
    plt.plot(bins, inv_gamma_pdf, linewidth=2, color='r')
    
    plt.show()
    plt.savefig("inverse_gamma_pdf_and_data_histogram.png")
    plt.clf()

plotDataHistogramAndInverseGammaPDF(pathToFile) 

# By looking at the plots, I can see that Gamma distribution seems to fit data fairly well 
# for large values in the data
# Inverse Gamma distribution seems to fit data fairly well for all the values in the data 
# Overall, Inverse Gamma distribution fits data much better and explains observed data much better



##################   3C   ##########################





        

    