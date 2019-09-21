/**
 * Prevents the following error when executing {@code gradle clean test}
 * <pre>
 * {@code
 *  Execution failed for task ':compileGroovy'.
 *  > org/apache/ivy/plugins/resolver/DependencyResolver}
 * </pre>
 * The above error is caused due to the @Grab statements inside Groovy scripts and classes.
 * With the below configuration we instruct groovyc to ignore those @Grab statements.
 */
withConfig(configuration) {
    configuration.setDisabledGlobalASTTransformations(['groovy.grape.GrabAnnotationTransformation'] as Set)
}