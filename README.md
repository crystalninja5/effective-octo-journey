# Motif

## The Basics

This is a Motif Scope. It doesn't do anything yet, but it will serve as a container for the objects you create:
<details>
<summary>Notes for Dagger users...</summary>
A Motif Scope is analogous to a Dagger `@Component`.
</details>

```java
@motif.Scope
interface MainScope {}
```

A nested `Objects` class will hold all of your factory methods. Motif recognizes the special `Objects` class by its name:
<details>
<summary>Notes for Dagger users...</summary>
The nested `Objects` class is just like a Dagger `@Module` except Motif only allows you to define one `Objects` class per Scope.
</details>

```java
@motif.Scope
interface MainScope {

    class Objects {}
}
```

To tell Motif how to create a `Controller` class, declare a factory method inside the `Objects` class:
<details>
<summary>Notes for Dagger users...</summary>
`Objects` factory methods are roughly the same as Dagger `@Provides` methods.
</details>

```java
@motif.Scope
interface MainScope {

    class Objects {

        Constroller controller() {
            return new Controller();
        }
    }
}
```

If your `Controller` needs a `View`, tell Motif how to create that as well, and pass the `View` in as a parameter to the controller factory method:
<details>
<summary>Notes for Dagger users...</summary>
Similar to Dagger `@Modules`, you can accept any type provided by a factory method as a parameter to other factory methods.
</details>

```java
@motif.Scope
interface MainScope {

    class Objects {

        View view() {
            return new View();
        }

        Constroller controller(View view) {
            return new Controller(view);
        }
    }
}
```

Maybe your `Controller` needs a `Database` too. Declare it like you declared the `View` and pass it in as a second parameter to the `Controller` factory method:

```java
@motif.Scope
interface MainScope {

    class Objects {

        View view() {
            return new View();
        }
        
        Database database() {
            return new Database();
        }

        Constroller controller(View view, Database database) {
            return new Controller(view, database);
        }
    }
}
```

If you compile the code above, Motif will generate a class in the same package called `MainScopeImpl` that implements your `MainScope` interface. Internally, the `MainScopeImpl` class knows all about the factory methods you've defined in your `Objects` class:
<details>
<summary>Notes for Dagger users...</summary>
The generated `*Impl` classes are similar to Dagger's generated `Dagger*` `Component` implementation classes.
</details>

```java
MainScope mainScope = new MainScopeImpl();
```

But the `MainScope` interface is empty which is not very useful. To expose a way to instantiate your controller class, define a parameterless method with return type `Controller` on the top level `MainScope` interface:
<details>
<summary>Notes for Dagger users...</summary>
The `controller()` method below is analogous to a Dagger `Component` [provision method](https://google.github.io/dagger/api/2.14/dagger/Component.html#provision-methods).
</details>

```java
@motif.Scope
interface MainScope {

    Controller controller();

    class Objects {

        View view() {
            return new View();
        }
        
        Database database() {
            return new Database();
        }

        Constroller controller(View view, Database database) {
            return new Controller(view, database);
        }
    }
}
```

Finally, instantiate a `Controller` by calling the `controller` method you just defined. Under the hood, `MainScopeImpl` resolves the `Controller`'s dependencies and calls the appropriate factory methods automatically:

```java
MainScope mainScope = new MainScopeImpl();
Controller controller = mainScope.controller();
```

Now that you can instantiate a `Controller`, let's make your factory methods a bit more concise. Factory methods that simply pass parameters through to a constructor without modification can be converted to parameterless abstract methods. For example, you can update the `controller` method to the following:
<details>
<summary>Notes for Dagger users...</summary>
This feature is similar to Dagger's `@Inject` constructor injection, but it doesn't require annotating the class' constructor, and it scopes the object to the enclosing Motif Scope.
</details>

```java
@motif.Scope
interface MainScope {

    Controller controller();

    abstract class Objects {

        View view() {
            return new View();
        }
        
        Database database() {
            return new Database();
        }

        abstract Constroller controller();
    }
}
```

The code above is equivalent to the previous version. Motif simply looks at the `Controller`'s constructor to determine its dependencies and how to instantiate it. You can do the same for `View` and `Database`:

```java
@motif.Scope
interface MainScope {

    Controller controller();

    abstract class Objects {

        abstract View view();

        abstract Database database();

        abstract Constroller controller();
    }
}
```

## Child Scopes

It might be useful to 