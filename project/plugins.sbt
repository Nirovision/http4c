scalacOptions += "-deprecation"

resolvers += Resolver.url("ambiata-oss", new URL("https://ambiata-oss.s3.amazonaws.com"))(Resolver.ivyStylePatterns)

resolvers += "Era7 maven releases" at "http://releases.era7.com.s3.amazonaws.com"

resolvers += Resolver.sonatypeRepo("releases")

resolvers += Resolver.typesafeRepo("releases")

addSbtPlugin("com.ambiata" % "promulgate" % "0.11.0-20160104104535-e21b092")

addSbtPlugin("com.orrsella" % "sbt-stats" % "1.0.5")
