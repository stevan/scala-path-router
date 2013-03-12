package com.iinteractive.web

import com.iinteractive.test._

import PathRouter._
import PathRouter.Method._

class PathRouterTestSuite001 extends TestMore {

    val fooRouter = new Router {
        def matcher = {
            case Route(GET, url"/foo")            => new Target () { def result = "/foo" }
            case Route(GET, url"/foo/bar")        => new Target () { def result = "/foo/bar" }
            case Route(GET, url"/foo/bar/$x")     => new Target (x) {
                def result = "/foo/bar/" + binding(0)
            }
            case Route(GET, url"/foo/bar/$x/baz") => new Target (x) {
                def result = "/foo/bar/" + binding(0)  + "/baz"
            }
        }
    }

    ok(!fooRouter.checkUri(GET, "/bar").isDefined, "... no match (/bar)")
    ok(!fooRouter.checkUri(GET, "/bar/foo").isDefined, "... no match (/bar/foo)")
    ok(!fooRouter.checkUri(GET, "/foo/baz").isDefined, "... no match (/foo/baz)")
    ok(!fooRouter.checkUri(GET, "/foo/bar/10/bar").isDefined, "... no match (/foo/bar/10/bar)")
    ok(!fooRouter.checkUri(GET, "/foo/bar/10/baz/bar").isDefined, "... no match (/foo/bar/10/baz/bar)")

    is(
        fooRouter.checkUri(GET, "/foo").get,
        "/foo",
        "... matched okay and got the uri we expected (/foo)"
    )
    is(
        fooRouter.matchUri(GET, "/foo").get.result.asInstanceOf[String],
        "/foo",
        "... got the expected result (/foo)"
    )

    is(
        fooRouter.checkUri(GET, "/foo/bar").get,
        "/foo/bar",
        "... matched okay and got the uri we expected (/foo/bar)"
    )
    is(
        fooRouter.matchUri(GET, "/foo/bar").get.result.asInstanceOf[String],
        "/foo/bar",
        "... got the expected result (/foo/bar)"
    )

    // test a couple of variants of the variable capture

    is(
        fooRouter.checkUri(GET, "/foo/bar/10").get,
        "/foo/bar/10",
        "... matched okay and got the uri we expected (/foo/bar/10)"
    )
    is(
        fooRouter.matchUri(GET, "/foo/bar/10").get.result.asInstanceOf[String],
        "/foo/bar/10",
        "... got the expected result (/foo/bar/10)"
    )

    is(
        fooRouter.checkUri(GET, "/foo/bar/baz").get,
        "/foo/bar/baz",
        "... matched okay and got the uri we expected (/foo/bar/baz)"
    )
    is(
        fooRouter.matchUri(GET, "/foo/bar/baz").get.result.asInstanceOf[String],
        "/foo/bar/baz",
        "... got the expected result (/foo/bar/baz)"
    )

    is(
        fooRouter.checkUri(GET, "/foo/bar/baz.html").get,
        "/foo/bar/baz.html",
        "... matched okay and got the uri we expected (/foo/bar/baz.html)"
    )
    is(
        fooRouter.matchUri(GET, "/foo/bar/baz.html").get.result.asInstanceOf[String],
        "/foo/bar/baz.html",
        "... got the expected result (/foo/bar/baz.html)"
    )    

    // test the mid-URI variable capture 

    is(
        fooRouter.checkUri(GET, "/foo/bar/10/baz").get,
        "/foo/bar/10/baz",
        "... matched okay and got the uri we expected (/foo/bar/10/baz)"
    )
    is(
        fooRouter.matchUri(GET, "/foo/bar/10/baz").get.result.asInstanceOf[String],
        "/foo/bar/10/baz",
        "... got the expected result (/foo/bar/10/baz)"
    )

    is(
        fooRouter.checkUri(GET, "/foo/bar/goorch/baz").get,
        "/foo/bar/goorch/baz",
        "... matched okay and got the uri we expected (/foo/bar/goorch/baz)"
    )
    is(
        fooRouter.matchUri(GET, "/foo/bar/goorch/baz").get.result.asInstanceOf[String],
        "/foo/bar/goorch/baz",
        "... got the expected result (/foo/bar/goorch/baz)"
    )

    is(
        fooRouter.checkUri(GET, "/foo/bar/goorch.bling/baz").get,
        "/foo/bar/goorch.bling/baz",
        "... matched okay and got the uri we expected (/foo/bar/goorch.bling/baz)"
    )
    is(
        fooRouter.matchUri(GET, "/foo/bar/goorch.bling/baz").get.result.asInstanceOf[String],
        "/foo/bar/goorch.bling/baz",
        "... got the expected result (/foo/bar/goorch.bling/baz)"
    )

}

