import sys

from setuptools import Extension, setup

extension_compile_args = ["/std:c11", "/experimental:c11atomics"] if sys.platform == "win32" else []

setup(
    name="xtables",
    version="0.1.0",
    python_requires=">=3.10",
    packages=["xtables"],
    package_data={ "xtables": ["py.typed", "*.pyi", "*.dll", "*.dylib", "*.so"] },
    ext_modules=[
        Extension(
            "xtables._native",
            sources=["xtables/_native.c"],
            extra_compile_args=extension_compile_args,
        ),
    ],
    zip_safe=False,
)
