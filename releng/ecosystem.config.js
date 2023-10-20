module.exports = {
  apps: [
    {
      name: 'luna',
      script: '/usr/lib/jvm/java-17-openjdk-amd64/bin/java',
      args: '-jar luna.jar',
      exp_backoff_restart_delay: 100,
    },
  ],
};
