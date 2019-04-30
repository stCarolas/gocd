# -*- mode: ruby -*-
# vi: set ft=ruby :

# All Vagrant configuration is done below. The "2" in Vagrant.configure
# configures the configuration version (we support older styles for
# backwards compatibility). Please don't change it unless you know what
# you're doing.
Vagrant.configure(2) do |config|
  config.vm.synced_folder "~/.m2", "/home/vagrant/.m2"
  config.vm.synced_folder "~/.gradle", "/home/vagrant/.gradle"
  config.vm.synced_folder "~/.npm", "/home/vagrant/.npm"

  packages = []

  packages << 'java-1.8.0-openjdk-devel'
  packages << %w(zip unzip gzip bzip2) # compression tools
  packages << %w(git)
  packages << %w(createrepo yum-utils rpm-build yum-utils) #for building rpm packages
  packages << %w(dpkg-devel dpkg-dev) #for building debian packages
  packages << %w(mingw32-nsis) #for building windows exe packages
  packages << %w(wget curl) #for downloading stuff
  packages << %w(nodejs-devel yarn) #nodejs for compiling rails assets
  packages << %w(devtoolset-6-gcc-c++ devtoolset-6-gcc) #to compile stuff via rubygems, npm etc
  packages << %w(rh-ruby23 rh-ruby23-ruby-devel rh-ruby23-rubygem-bundler rh-ruby23-ruby-irb rh-ruby23-rubygem-rake rh-ruby23-rubygem-psych libffi-devel) #ruby needed for fpm

  config.vm.provision :shell, inline: <<-SHELL
  set -ex
  echo 'sslverify=false' >> /etc/yum.conf
  echo '
[nsis]
name=nsis
baseurl=http://gocd.github.io/nsis-rpm/
gpgcheck=0
sslverify=0
' > /etc/yum.repos.d/nsis.repo

  echo '
[nodesource]
name=Node.js Packages for Enterprise Linux $releasever - $basearch
baseurl=http://rpm.nodesource.com/pub_6.x/el/$releasever/$basearch
enabled=1
gpgcheck=1
gpgkey=http://rpm.nodesource.com/pub/el/NODESOURCE-GPG-SIGNING-KEY-EL
sslverify=0
' > /etc/yum.repos.d/nodesource.repo

  wget -q http://dl.yarnpkg.com/rpm/yarn.repo -O /etc/yum.repos.d/yarn.repo

  yum install -y centos-release-scl
  yum install -y #{packages.flatten.join(' ')}

  echo 'source /opt/rh/rh-ruby23/enable' > /etc/profile.d/ruby-23.sh
  echo 'export PATH=/opt/rh/rh-ruby23/root/usr/local/bin:$PATH' >> /etc/profile.d/ruby-23.sh

  # remove a sudo binary that messes things up
  rm -f /opt/rh/devtoolset-6/root/usr/bin/sudo

  echo 'source /opt/rh/devtoolset-6/enable' > /etc/profile.d/gcc.sh
  echo 'PATH=$PATH:/opt/rh/devtoolset-6/root/usr/bin' >> /etc/profile.d/gcc.sh

  (source /etc/profile.d/ruby-23.sh; source /etc/profile.d/gcc.sh; gem install fpm --no-ri --no-rdoc)
  SHELL

  config.vm.provider :virtualbox do |vb, override|
    override.vm.box = "bento/centos-6.7"

    vb.gui          = ENV['GUI'] || false
    vb.memory       = ((ENV['MEMORY'] || 4).to_f * 1024).to_i
    vb.cpus         = 4
  end

  if Vagrant.has_plugin?('vagrant-cachier')
    config.cache.scope = :box
    config.cache.enable :apt
    config.cache.enable :apt_lists
    config.cache.enable :yum
    config.cache.enable :npm
  end

  if Vagrant.has_plugin?('vagrant-vbguest')
    config.vbguest.auto_update = false
  end

end
