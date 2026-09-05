"""Python client for the xtables key/value server.

Poses cross the wire in WPILib's struct layout. The four pose calls below are
rebound so they take and return WPILib's own geometry types, matching the Java
client; :mod:`xtables.geometry` converts between those and the wire types.
"""

from .xtables import *  # noqa: F401,F403
from .xtables import XTablesClient
from . import geometry


def _converting_reader(read):
    def method(self, channel):
        pose = read(self, channel)
        return None if pose is None else geometry.convert(pose)

    method.__name__ = read.__name__
    method.__doc__ = read.__doc__
    return method


def _converting_writer(write):
    def method(self, channel, value):
        write(self, channel, geometry.convert(value))

    method.__name__ = write.__name__
    method.__doc__ = write.__doc__
    return method


XTablesClient.get_pose2d = _converting_reader(XTablesClient.get_pose2d)
XTablesClient.get_pose3d = _converting_reader(XTablesClient.get_pose3d)
XTablesClient.put_pose2d = _converting_writer(XTablesClient.put_pose2d)
XTablesClient.put_pose3d = _converting_writer(XTablesClient.put_pose3d)
