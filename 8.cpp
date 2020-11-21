// 常量替换
#include <stdio.h>
#include <string.h>
#define N 20
#define M 100
#define Q 25
#define zero 0
using namespace std;
int n,sum,a[N];
bool b[M]={0},c[M]={0},d[M]={0};
char MAP[Q][Q];
void print()
{
    sum++;
    if(sum<=1)
    {

        for(int i=1;i<=n;i++){
            MAP[i][a[i]]='Q';
        }
    }
}
void queen(int i)
{
    for(int j=1;j<=n;j++)
    {
       if((b[j]==0)&&(c[i+j]==0)&&(d[i-j+n]==0))
       {
        a[i]=j;
        b[j]=1;
        c[i+j]=1;
        d[i-j+n]=1;
        if(i==n)
            print();
        else
            queen(i+1);
        b[j]=zero;
        c[i+j]=zero;
        d[i-j+n]=zero;
        if(sum)
            break;
       }
    }
}
int main()
{
    while(~scanf("%d",&n)&&n){
        memset(MAP,'.',sizeof(MAP));
        memset(a,0,sizeof(a));
        memset(b,0,sizeof(b));
        memset(c,0,sizeof(c));
        memset(d,0,sizeof(d));
        sum=zero;
        queen(1);
        if(sum){
            for(int i=1;i<=n;i++){
                for(int j=1;j<=n;j++)
                    printf("%c ",MAP[i][j]);
                printf("\n");
            }
        }
        else
            printf("No answer.\n");
        printf("\n");
    }
    return 0;
}
