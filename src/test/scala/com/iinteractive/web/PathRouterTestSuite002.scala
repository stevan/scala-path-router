package com.iinteractive.web

import com.iinteractive.test._

import PathRouter._
import PathRouter.Method._

class PathRouterTestSuite002 extends TestMore {

    val fooRouter = Router {
        case Route(GET, url"/foo") => Target { "/foo" }
    }

    val barRouter = Router {
        case Route(GET, url"/bar") => Target { "/bar" }
    }

    val fooBarRouter = fooRouter orElse barRouter

    ok(fooBarRouter.checkUri(GET, "/foo").isDefined, "... matched (/foo)")
    ok(fooBarRouter.checkUri(GET, "/bar").isDefined, "... matched (/bar)") 
    ok(!fooBarRouter.checkUri(GET, "/baz").isDefined, "... no match (/baz)") 

    is(
        fooBarRouter.checkUri(GET, "/foo").get,
        "/foo",
        "... matched okay and got the uri we expected (/foo)"
    )
    is(
        fooBarRouter.matchUri(GET, "/foo").get.result.asInstanceOf[String],
        "/foo",
        "... got the expected result (/foo)"
    )

    is(
        fooBarRouter.checkUri(GET, "/bar").get,
        "/bar",
        "... matched okay and got the uri we expected (/bar)"
    )
    is(
        fooBarRouter.matchUri(GET, "/bar").get.result.asInstanceOf[String],
        "/bar",
        "... got the expected result (/bar)"
    )   

}

