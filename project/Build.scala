import sbt._
import com.github.siasia._
import WebappPlugin.webappSettings
import Keys._

object LemonSourceBuild extends Build {
  // This value should track with the current Monnet version
  val monnetVersion = "1.12.7-SNAPSHOT"                        
  
  // The Jetty server
  lazy val container = Container("container")

  // Settings for all projects
  lazy val sharedSettings = Seq(version in ThisBuild := "1.12.7",
                                scalaVersion in ThisBuild := "2.9.2",
                                resolvers in ThisBuild += "Monnet Maven Repository" at "http://monnet01.sindice.net/mvn/",
                                resolvers in ThisBuild += "Twitter Maven Repository" at "http://maven.twttr.com/")
                                
  override lazy val settings = super.settings ++ sharedSettings
  
  lazy val rootSettings = Seq(
    libraryDependencies += "org.mortbay.jetty" % "jetty" % "6.1.22" % "container"
  ) ++ container.deploy(
    "/lemonsource" -> web
  )
  
  // The root project
  lazy val root = Project(id = "lemonsource",
                          base = file(".")) aggregate(app,generation,journal,mvc,ui,users,web) settings(rootSettings:_*)
  
  // This project defines common classes
  lazy val app = Project(id = "app",
                         base = file("app"),
                         settings = Project.defaultSettings ++ Seq(libraryDependencies ++= Seq(
                               "eu.monnetproject" % "lemon.api" % monnetVersion,
                               "eu.monnetproject" % "lemon.oils" % monnetVersion,
                               "eu.monnetproject" % "core" % monnetVersion
                         ))) dependsOn(generation)

  // The project handles interaction with the lexicon generator
  lazy val generation = Project(id = "generation",
                                base = file("generation"),
                                settings = Project.defaultSettings ++ Seq(libraryDependencies ++= Seq(
                                      "eu.monnetproject" % "lemon.api" % monnetVersion,
                                      "eu.monnetproject" % "lemon.oils" % monnetVersion,
                                      "eu.monnetproject" % "core" % monnetVersion,
                                      "org.apache.httpcomponents" % "httpclient" % "4.1.2",
                                      "org.apache.httpcomponents" % "httpmime" % "4.1.2")
                                  ))
  
  // History and talk pages
  lazy val journal = Project(id = "journal",
                             base = file("journal")) dependsOn(app,ui)
  
  // The model-view-controller for the appp
  lazy val mvc = Project(id = "mvc",
                         base = file("mvc")) dependsOn(app,ui,journal)

  // The generic user interface definition
  lazy val ui = Project(id = "ui",
                                base = file("ui")) dependsOn(app)
  
  // Management of user, login handling etc.
  lazy val users = Project(id = "users",
                           base = file("users")) dependsOn(app,ui)

  // Most of the code to handle HTML and HTTP                            
  lazy val webSettings = webappSettings ++ Seq(libraryDependencies ++= Seq(
                               "org.mortbay.jetty" % "servlet-api" % "2.5.20110712" % "provided",
                               "commons-fileupload" % "commons-fileupload" % "1.2.2",
                               "commons-io" % "commons-io" % "2.1",
                               "commons-lang" % "commons-lang" % "2.6",
                               "eu.monnetproject" % "framework.services" % monnetVersion,
                               "eu.monnetproject" % "nlp.owlapi" % monnetVersion,
                               "eu.monnetproject" % "kap.laif" % monnetVersion
                         ))
                           
  lazy val web = Project(id = "web",
                         base = file("web")) dependsOn(mvc,users) settings(webSettings:_*)
                         
  override def projects = Seq(root,app,generation,journal,mvc,ui,users,web)
}
