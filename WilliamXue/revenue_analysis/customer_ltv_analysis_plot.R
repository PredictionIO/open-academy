customer_value <- read.csv(
  file="../../../data/processed/positiveSixMonthRevenue.csv", 
  sep=",", 
  head=TRUE)

# PLOT DISTRIBUTIONS OVER THE DATA

# Plot gamma distribution
dev.new()
hist(
  customer_value$revenue,
  freq=FALSE,
  breaks=100,
  main="Histogram of Positive Customer Values \n (Gamma)",
  xlim=c(0,2500), 
  xlab="Customer Value")

gammaLikelihood <- function(par) {
  k <- par[1]
  theta <- par[2]
  X <- customer_value$revenue
  return (length(X)*(-k*log(theta) - log(gamma(k))) - sum(X)/theta + (k-1)*sum(log(X)))
}

gamma_parameters = optim(par=c(k=1,theta=0.1), fn=gammaLikelihood, method="L-BFGS-B", control=c(fnscale=-1), lower=0.0001)$par
print(gamma_parameters)

curve(
  dgamma(x, shape=gamma_parameters["k"], scale=gamma_parameters["theta"]),
  add=TRUE,
  col="red")


# Plot inverse gamma distribution
dev.new()
hist(
  customer_value$revenue,
  freq=FALSE,
  breaks=100,
  main="Histogram of Positive Customer Values \n (Inverse Gamma)",
  xlim=c(0,2500), 
  xlab="Customer Value")

invGamma <- function(x, alpha, beta) {
  (beta^alpha/gamma(alpha)) * x^(-alpha-1) * exp(-beta/x)
}

invGammaLikelihood <- function(par) {
  alpha <- par[1]
  beta <- par[2]
  X <- customer_value$revenue
  return (length(X)*(alpha*log(beta) - log(gamma(alpha))) + (-alpha - 1)*sum(log(X)) - beta*sum(sapply(X, FUN=function(x) 1/x)))
}

inv_gamma_parameters <- optim(par=c(alpha=1,beta=0.1), fn=invGammaLikelihood, method="L-BFGS-B", control=c(fnscale=-1), lower=0.0001)$par
print(inv_gamma_parameters)

curve(
  invGamma(x, alpha=inv_gamma_parameters["alpha"], beta=inv_gamma_parameters["beta"]),
  add=TRUE,
  col="red")


log_customer_revenue <- sapply(customer_value$revenue, FUN=function(x) log(x))

# PLOT DISTRIBUTIONS OVER THE LOG OF THE DATA

# Plot gamma distribution over log of data
dev.new()
hist(
  log_customer_revenue,
  freq=FALSE,
  main="Histogram of the Logarithm of Positive Customer Values \n (Gamma)",
  xlab="log(Customer Value)")

logGamma <- function(x, k, theta) {
  exp(x) * (1/(theta^k * gamma(k))) * (exp(x))^(k-1) * exp(-exp(x)/theta)
}

curve(
  logGamma(x, k=gamma_parameters["k"], theta=gamma_parameters["theta"]),
  add=TRUE,
  col="red")


# Plot inverse gamma distribution over log of data
dev.new()
hist(
  log_customer_revenue,
  freq=FALSE,
  main="Histogram of the Logarithm of Positive Customer Values \n (Inverse Gamma)",
  xlab="log(Customer Value)")

invLogGamma <- function(x, alpha, beta) {
  exp(x) * (beta^alpha/gamma(alpha)) * (exp(x))^(-alpha-1) * exp(-beta/exp(x))
}

curve(
  invLogGamma(x, alpha=inv_gamma_parameters["alpha"], beta=inv_gamma_parameters["beta"]),
  add=TRUE,
  col="red")

