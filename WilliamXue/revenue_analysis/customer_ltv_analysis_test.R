customer_value <- read.csv(
  file="../../../data/processed/positiveSixMonthRevenue.csv", 
  sep=",", 
  head=TRUE)

sample_size <- length(customer_value$revenue)

# MLE Parameters
invGammaLikelihood <- function(par) {
  alpha <- par[1]
  beta <- par[2]
  X <- customer_value$revenue
  return (length(X)*(alpha*log(beta) - log(gamma(alpha))) + (-alpha - 1)*sum(log(X)) - beta*sum(sapply(X, FUN=function(x) 1/x)))
}

inv_gamma_parameters <- optim(par=c(alpha=1,beta=0.1), fn=invGammaLikelihood, method="L-BFGS-B", control=c(fnscale=-1), lower=0.0001)$par
alpha = inv_gamma_parameters["alpha"]
beta = inv_gamma_parameters["beta"]

# Anderson Darling Test

# Helper functions for the Anderson Darling Test
invGammaCDF <- function(x, alpha, beta) {
  pgamma(beta/x, alpha, lower=FALSE)
}

calculateAndersonDarlingTestStat <- function(sample) {
  sum <- 0
  for (i in 1:length(sample)) {
    x <- sample[i]
    sum <- sum + (2*i-1)/length(sample) * (log(invGammaCDF(x, alpha, beta)) + log(1 - invGammaCDF(x, alpha, beta)))
  }

  test_statistic <- (-length(sample) - sum)
}

# Perform the Anderson Darling Test
anderson_darling_dist_stats <- rep(NaN, 1000)
for (i in 1:length(anderson_darling_dist_stats)) {
  sample <- 1/rgamma(sample_size, shape=alpha, rate=beta)
  anderson_darling_dist_stats[i] <- calculateAndersonDarlingTestStat(sample)
}

samples_95th_percentile_test_stat <- quantile(anderson_darling_dist_stats, probs=0.95)

customer_data_test_stat <- calculateAndersonDarlingTestStat(customer_value$revenue)
 
reject_null_hypothesis <- (customer_data_test_stat > samples_95th_percentile_test_stat)
print(paste("95th percentile stat:", toString(samples_95th_percentile_test_stat), sep=" "))
print(paste("Data stat:", toString(customer_data_test_stat), sep=" "))
print(reject_null_hypothesis)