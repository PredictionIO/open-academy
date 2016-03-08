plotNormal <- function(num_samples) {
  normal_samples = rnorm(n=num_samples)

  dev.new()
  hist(
    normal_samples,
    freq=FALSE,
    main=paste("Normal(0, 1) Sample of Size", num_samples, sep=" "),
    xlim=c(-10,10), xlab="x",
    ylab="p(x | 0, 1)")

  curve(
    dnorm(x, mean=mean(normal_samples), sd=sd(normal_samples)),
    add=TRUE,
    col="red")
}

plotGamma <- function(num_samples) {
  standard_shape = 1
  gamma_samples = rgamma(n=num_samples, shape=standard_shape)

  dev.new()
  hist(
    gamma_samples,
    freq=FALSE,
    main=paste("Gamma(1, 1) Sample of Size", num_samples, sep=" "),
    xlim=c(0,20), xlab="x",
    ylab="p(x | 1, 1)")

  curve(
    dgamma(x, shape=standard_shape),
    add=TRUE,
    col="red")
}

plotSamples <- function() {
  # Plot the normal distribution for various sample sizes.
  for (i in list(20, 50, 100, 500)) {
    plotNormal(i)
  }

  # Plot the gamma distribution for various sample sizes.
  for (i in list(20, 50, 100, 500)) {
    plotGamma(i)
  }
}

plotSamples()
