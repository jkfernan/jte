import org.jenkinsci.plugins.workflow.cps.CpsClosure2

void call(Map args = [yaml: resource('default_pod.yaml'), containerName: 'default'], CpsClosure2 body) {
  validKeys = ['yaml', 'containerName']
  invalidKeys = args.findAll { mapItem -> !(mapItem.key in validKeys) } .keySet()
  if (invalidKeys) {
    throw new IllegalArgumentException(
      "Invalid key ${invalidKeys} passed to on_merge, valid keys: ${validKeys}"
    )
  }
  if (args.size() != 2 || !('yaml' in args.keySet()) || !('containerName' in args.keySet())) {
    throw new IllegalArgumentException(
      'pod_container step requires 2 arguments: yaml and containerName'
    )
  }

  podTemplate(yaml: args.yaml) {
    node(POD_LABEL) {
      container(args.containerName) {
        body()
      }
    }
  }
}
