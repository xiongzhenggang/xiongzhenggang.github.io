#!/usr/bin/env python3
# -*- coding: utf-8 -*-
class TestEval(object):
    
    def __init___(self,*args):
        self.args=args
    
    def __repr__(self):

        return '\n\n'.join(a+'\n'+repr(eval(a)) for a in self.args)
        