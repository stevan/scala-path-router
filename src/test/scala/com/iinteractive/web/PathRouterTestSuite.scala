package com.iinteractive.web

import com.iinteractive.test._

import PathRouter._
import PathRouter.Method._

// Target should be easily overridden
// to make simple custom classes
class HelloFromFoo extends Target() {
    def result = "Hello From foo\n"
}

// Target can also be overridden to
// take specific parameters as well
class HelloFromFooBarX (private val a: String) extends Target() {
    def result = "Hello From foo/bar/" + a + "\n"
}

class PathRouterTestSuite extends TestMore {

    // creation of routers is
    // very straight forward
    val fooRouter = Router {
        case Route(GET, url"/foo")            => new HelloFromFoo()
        case Route(GET, url"/foo/bar/$x")     => new HelloFromFooBarX (x)
        case Route(GET, url"/foo/bar")        => Target { "Hello From foo/bar\n" }
        case Route(GET, url"/foo/bar/$x/baz") => new Target (x) {
            def result = ">>Hello From foo/bar/" + binding(0)  + "/baz\n"
        }
    }

    // having a catch all router
    // is recommended
    val catchAllRouter = Router {
        case _ => Target { "404 - Not Found\n" }
    }

    // at any time you can check
    // to see if a uri is valid
    // for that particular router
    is(fooRouter.checkUri(GET, "/foo/bar/100").get, "/foo/bar/100", "... got the Uri we expected")

    // routers can easily be chained
    val allRoutes = fooRouter orElse catchAllRouter

    // matching
    val response = allRoutes.matchUri(
        GET, "/foo/bar/10"
    ).get.result

    //diag(response.asInstanceOf[String])
    is(response.asInstanceOf[String], "Hello From foo/bar/10\n", "... got the expected response")

}

