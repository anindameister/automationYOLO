import argparse
# positional,optional

if __name__ == "__main__":
    parser=argparse.ArgumentParser() #ArgumentParser is the class of the object(module)argparse
    parser.add_argument("number1",help="first number")
    parser.add_argument("number2",help="first number")
    parser.add_argument("operation",help="first number")

    args=parser.parse_args()
print(args.number1)
print(args.number2)
print(args.operation)

